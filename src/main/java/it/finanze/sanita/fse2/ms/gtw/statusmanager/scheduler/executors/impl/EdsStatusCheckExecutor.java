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
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionDataETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
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

            log.info("[EDS-STATUS-CHECK] Received status from EDS: workflowInstanceId={}, status={}, type={}", 
                    workflowInstanceId, statusResponse.getStatus(), statusResponse.getType());

            // Save final status event
            transactionRepo.saveEdsEvent(
                    workflowInstanceId,
                    new Date(),
                    TransactionDataETY.FHIR_TYPE_UAR,
                    statusResponse.getStatus()
            );

            log.info("[EDS-STATUS-CHECK] Successfully updated transaction status: workflowInstanceId={}, finalStatus={}", 
                    workflowInstanceId, statusResponse.getStatus());

            return true;

        } catch (Exception ex) {
            log.error("[EDS-STATUS-CHECK] Failed to process transaction: workflowInstanceId={}", 
                    workflowInstanceId, ex);
            return false;
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
     * Retry helper with exponential backoff
     */
    private <T> T retry(String label, Supplier<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                T result = operation.get();
                if (result != null) {
                    return result;
                }
            } catch (Exception ex) {
                lastException = ex;
                log.warn("[EDS-STATUS-CHECK] {} attempt {} failed: {}", label, attempt, ex.getMessage());
            }
            
            if (attempt < MAX_RETRIES) {
                backoff(label, attempt);
            }
        }
        
        if (lastException != null) {
            log.error("[EDS-STATUS-CHECK] {} failed after {} attempts", label, MAX_RETRIES, lastException);
            throw new RuntimeException("Failed after " + MAX_RETRIES + " attempts", lastException);
        }
        
        return null;
    }

    /**
     * Exponential backoff between retries
     */
    private void backoff(String label, int attempt) {
        long sleepMs = BASE_BACKOFF_MS << (attempt - 1);
        log.debug("[EDS-STATUS-CHECK] {} backing off for {} ms before retry", label, sleepMs);
        
        try {
            TimeUnit.MILLISECONDS.sleep(sleepMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("[EDS-STATUS-CHECK] Backoff interrupted for {}", label);
        }
    }
}
