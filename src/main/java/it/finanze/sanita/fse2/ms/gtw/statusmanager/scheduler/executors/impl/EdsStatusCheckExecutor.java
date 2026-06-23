/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.eds.GetIngestionStatusResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.RemoteServiceNotAvailableException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionDataETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class EdsStatusCheckExecutor {

    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 2_000;

    private final IEdsClient edsClient;
    private final ITransactionEventsRepo transactionRepo;

    @Value("${scheduler.eds-status-check.event-age-threshold-minutes}")
    private int eventAgeThresholdMinutes;

    private final int MAX_RESULT = 1000;

    public EdsStatusCheckExecutor(IEdsClient edsClient, ITransactionEventsRepo transactionRepo) {
        this.edsClient = edsClient;
        this.transactionRepo = transactionRepo;
    }

    @Scheduled(cron = "${scheduler.tx-scheduler}")
    @SchedulerLock(name = "edsStatusCheckScheduler", lockAtMostFor = "15m", lockAtLeastFor = "1m")
    public void run() {
        log.info("[EDS-STATUS-CHECK] Starting EDS status check scheduler");

        try {
            // Calculate threshold date (current time - configured minutes)
            Date thresholdDate = calculateThresholdDate();
            log.info("[EDS-STATUS-CHECK] Checking transactions older than: {} (max results: {})", thresholdDate, MAX_RESULT);

            // Find pending UAR transactions
            List<TransactionDataETY> pendingTransactions = transactionRepo.findPendingUarTransactions(thresholdDate, MAX_RESULT);
            log.info("[EDS-STATUS-CHECK] Found {} pending transactions to check", pendingTransactions.size());

            if (pendingTransactions.isEmpty()) {
                log.info("[EDS-STATUS-CHECK] No pending transactions found. Scheduler completed.");
                return;
            }

            // Process each transaction
            int successCount = 0;
            int failureCount = 0;

            for (TransactionDataETY transaction : pendingTransactions) {
                try {
                    boolean success = processTransaction(transaction);
                    if (success) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception ex) {
                    log.error("[EDS-STATUS-CHECK] Error processing transaction with workflowInstanceId: {}", 
                            transaction.getWorkflowInstanceId(), ex);
                    failureCount++;
                }
            }

            log.info("[EDS-STATUS-CHECK] Scheduler completed: success={}, failed={}, total={}", 
                    successCount, failureCount, pendingTransactions.size());

        } catch (Exception ex) {
            log.error("[EDS-STATUS-CHECK] Fatal error in scheduler execution", ex);
        }
    }

    /**
     * Process a single transaction: call EDS client and update status
     */
    private boolean processTransaction(TransactionDataETY transaction) {
        String workflowInstanceId = transaction.getWorkflowInstanceId();
        log.debug("[EDS-STATUS-CHECK] Processing transaction: workflowInstanceId={}, eventDate={}",
                workflowInstanceId, transaction.getDate());

        try {
            // Call EDS client with retry mechanism
            GetIngestionStatusResDTO statusResponse = retry(
                    "GET-STATUS-" + workflowInstanceId,
                    () -> edsClient.getIngestionStatus(workflowInstanceId)
            );

            if (statusResponse == null) {
                log.warn("[EDS-STATUS-CHECK] No response from EDS client for workflowInstanceId: {}", workflowInstanceId);
                return false;
            }

            log.info(
                    "[EDS-STATUS-CHECK] Received status from EDS: workflowInstanceId={}, status={}, type={}, message={}",
                    workflowInstanceId, statusResponse.getStatus(), statusResponse.getType(),
                    statusResponse.getMessage());

            transactionRepo.saveEdsEvent(
                    workflowInstanceId,
                    new Date(),
                    TransactionDataETY.FHIR_TYPE_UAR,
                    statusResponse.getStatus(),
                    statusResponse.getMessage()
            );

            log.info(
                    "[EDS-STATUS-CHECK] Successfully updated transaction status: workflowInstanceId={}, finalStatus={}",
                    workflowInstanceId, statusResponse.getStatus());

            return true;

        } catch (RemoteServiceNotAvailableException ex) {
            // Communication error after all retries exhausted
            log.error("[EDS-STATUS-CHECK] Communication failure after {} retries for workflowInstanceId: {}",
                    MAX_RETRIES, workflowInstanceId, ex);

            // Mark transaction as blocked in database
            markTransactionAsBlocked(workflowInstanceId, "Communication failure after retries");
            return false;

        } catch (BusinessException ex) {
            // Non-retryable business/validation error
            log.error("[EDS-STATUS-CHECK] Non-retryable error for workflowInstanceId: {}",
                    workflowInstanceId, ex);

            // Mark transaction as blocked in database
            markTransactionAsBlocked(workflowInstanceId, "Non-retryable business error");
            return false;

        } catch (Exception ex) {
            // Unexpected error
            log.error("[EDS-STATUS-CHECK] Unexpected error processing transaction: workflowInstanceId={}",
                    workflowInstanceId, ex);

            // Mark transaction as blocked in database
            markTransactionAsBlocked(workflowInstanceId, "Unexpected error");
            return false;
        }
    }

    /**
     * Mark a transaction as blocked by updating the pullStatusOutcome field.
     * This is called when EDS status check fails due to communication errors or
     * non-retryable errors.
     *
     * @param workflowInstanceId The workflow instance identifier
     * @param reason             The reason for blocking (for logging purposes)
     */
    private void markTransactionAsBlocked(String workflowInstanceId, String reason) {
        try {
            log.info("[EDS-STATUS-CHECK] Marking transaction as blocked: workflowInstanceId={}, reason={}",
                    workflowInstanceId, reason);

            boolean updated = transactionRepo.updatePullStatusOutcome(workflowInstanceId,
                    TransactionDataETY.PULL_STATUS_BLOCKED);

            if (updated) {
                log.info("[EDS-STATUS-CHECK] Successfully marked transaction as blocked: workflowInstanceId={}",
                        workflowInstanceId);
            } else {
                log.warn(
                        "[EDS-STATUS-CHECK] Failed to mark transaction as blocked (no matching event found): workflowInstanceId={}",
                        workflowInstanceId);
            }

        } catch (Exception ex) {
            log.error("[EDS-STATUS-CHECK] Error marking transaction as blocked: workflowInstanceId={}",
                    workflowInstanceId, ex);
            // Don't rethrow - this is a best-effort operation
        }
    }

    /**
     * Calculate threshold date based on configured minutes.
     * Uses UTC to ensure consistency with MongoDB date storage.
     */
    private Date calculateThresholdDate() {
        Instant now = Instant.now();  // Always UTC
        Instant threshold = now.minus(eventAgeThresholdMinutes, ChronoUnit.MINUTES);
        return Date.from(threshold);
    }

    /**
     * Retry helper with exponential backoff that only retries communication errors.
     * Non-retryable errors (business logic, validation, etc.) fail immediately.
     *
     * @param <T>       The return type of the operation
     * @param label     Descriptive label for logging purposes
     * @param operation The operation to retry
     * @return The result of the successful operation
     * @throws BusinessException                  if operation fails with
     *                                            non-retryable error
     * @throws RemoteServiceNotAvailableException if all retry attempts fail for
     *                                            communication errors
     */
    private <T> T retry(String label, Supplier<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                T result = operation.get();

                // Validate result is not null
                if (result == null) {
                    log.warn("[EDS-STATUS-CHECK] {} attempt {} returned null result", label, attempt);
                    lastException = new IllegalStateException("Operation returned null result");

                    // Null result is retryable (might be transient issue)
                    if (attempt < MAX_RETRIES) {
                        backoff(label, attempt);
                        continue;
                    }
                } else {
                    // Success - log recovery if needed
                    if (attempt > 1) {
                        log.debug("[EDS-STATUS-CHECK] {} succeeded on attempt {}/{}", label, attempt, MAX_RETRIES);
                    }
                    return result;
                }

            } catch (Exception ex) {
                lastException = ex;

                // Classify exception to determine if retry is appropriate
                if (isRetryableException(ex)) {
                    log.warn("[EDS-STATUS-CHECK] {} attempt {}/{} failed with retryable error: {}",
                            label, attempt, MAX_RETRIES, ex.getMessage());
                    if (attempt < MAX_RETRIES) {
                        backoff(label, attempt);
                        continue;
                    }
                } else {
                    // Non-retryable error - fail immediately without retry
                    log.error("[EDS-STATUS-CHECK] {} failed with non-retryable error on attempt {}: {}",
                            label, attempt, ex.getMessage(), ex);
                    throw new BusinessException(
                            String.format("Operation '%s' failed with non-retryable error", label),
                            ex);
                }
            }
        }

        // All retries exhausted for communication errors
        String errorMsg = String.format(
                "Operation '%s' failed after %d retry attempts due to communication errors",
                label, MAX_RETRIES);
        log.error("[EDS-STATUS-CHECK] {}", errorMsg, lastException);
        throw new RemoteServiceNotAvailableException(errorMsg, lastException);
    }

    /**
     * Determines if an exception is retryable (communication/transient errors).
     *
     * Retryable exceptions include:
     * - Network/connection errors (IOException, SocketException, etc.)
     * - HTTP client errors (ResourceAccessException, RestClientException)
     * - Timeout exceptions
     * - Service unavailable (5xx errors)
     *
     * Non-retryable exceptions include:
     * - Business logic errors (BusinessException, IllegalArgumentException)
     * - Validation errors (4xx client errors except 408, 429)
     * - Authentication/authorization errors (401, 403)
     * - Not found errors (404)
     *
     * @param ex The exception to classify
     * @return true if the exception represents a retryable communication error
     */
    private boolean isRetryableException(Exception ex) {
        // Already classified as remote service issue
        if (ex instanceof RemoteServiceNotAvailableException) {
            return true;
        }

        // Business exceptions should not be retried
        if (ex instanceof BusinessException) {
            return false;
        }

        // Network/IO errors - retryable
        if (ex instanceof IOException ||
                ex instanceof SocketException ||
                ex instanceof SocketTimeoutException ||
                ex instanceof ConnectException ||
                ex instanceof UnknownHostException) {
            return true;
        }

        // HTTP client errors (4xx) - check status code
        if (ex instanceof HttpClientErrorException) {
            HttpClientErrorException httpEx = (HttpClientErrorException) ex;
            int statusCode = httpEx.getStatusCode().value();
            // 408 Request Timeout and 429 Too Many Requests are retryable
            return statusCode == 408 || statusCode == 429;
        }

        // HTTP server errors (5xx) - retryable
        if (ex instanceof HttpServerErrorException) {
            return true;
        }

        // Spring RestTemplate communication errors - retryable
        if (ex instanceof ResourceAccessException) {
            return true;
        }

        // Other RestClient exceptions - retryable
        if (ex instanceof RestClientException) {
            return true;
        }

        // Timeout exceptions - retryable
        if (ex instanceof TimeoutException) {
            return true;
        }

        // Circuit breaker exceptions - check by class name as a fallback
        if (ex.getClass().getSimpleName().equals("CircuitBreakerOpenException")) {
            return true;
        }
        
        // Default: non-retryable for unknown exceptions (fail-safe approach)
        log.debug("[EDS-STATUS-CHECK] Classifying unknown exception as non-retryable: {}", ex.getClass().getName());
        return false;
    }

    /**
     * Exponential backoff with jitter to prevent thundering herd.
     * Jitter adds 0-25% random variation to prevent synchronized retries across
     * multiple instances.
     *
     * @param label   Descriptive label for logging
     * @param attempt Current attempt number (1-based)
     */
    private void backoff(String label, int attempt) {
        // Calculate exponential backoff: 2^(attempt-1) * BASE_BACKOFF_MS
        long baseDelay = BASE_BACKOFF_MS << (attempt - 1);

        // Add jitter (0-25% of base delay) to prevent synchronized retries
        // This distributes retry load when multiple instances fail simultaneously
        long jitter = (long) (baseDelay * 0.25 * Math.random());
        long sleepMs = baseDelay + jitter;

        log.debug("[EDS-STATUS-CHECK] {} backing off for {} ms before retry {}",
                label, sleepMs, attempt + 1);
        
        try {
            TimeUnit.MILLISECONDS.sleep(sleepMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("[EDS-STATUS-CHECK] Backoff interrupted for {} during attempt {}", label, attempt);
            // Don't throw exception - let retry loop handle interruption
        }
    }
}
