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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionDataETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import lombok.extern.slf4j.Slf4j;

/**
 * Transaction Event service.
 */
@Service
@Slf4j
public class TransactionEventsSRV extends AbstractService implements ITransactionEventsSRV {

    private static final long serialVersionUID = 8384735139163560923L;
	
	@Autowired
    private transient ITransactionEventsRepo transactionEventsRepo;

    @Autowired
    @Qualifier("notxkafkatemplate")
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.statusmanager.finalstatus.topic}")
    private String finalStateTopic;

	@Override
    public void saveEvent(final String workflowInstanceId, final String json) {
        try {
            log.info("START - Save event for workflowInstanceId: {}", workflowInstanceId);

               // Parse JSON to extract eventType and eventStatus
               Document doc = Document.parse(json);
               String eventType = doc.getString("eventType");
               String eventStatus = doc.getString("eventStatus");

               transactionEventsRepo.saveEvent(workflowInstanceId, json);

               // Call notification with parsed values
               handleFinalStatusNotification(workflowInstanceId, eventType, eventStatus);

               log.info("END - Save event for workflowInstanceId: {}", workflowInstanceId);
           } catch (Exception ex) {
               log.error("Error while saving event for workflowInstanceId: {}", workflowInstanceId, ex);
               throw new BusinessException(ex);
           }
       }

    @Override
    public void saveEvent(CallbackTransactionDataRequestDTO request) {
        try {
            log.info("START - Save EDS event for workflowInstanceId: {}", request.getWorkflowInstanceId());
            transactionEventsRepo.saveEdsEvent(
                    request.getWorkflowInstanceId(),
                    request.getInsertionDate(),
                    TransactionDataETY.FHIR_TYPE_UAR,
                    request.getStatus(),
                    request.getMessage());
            log.info("END - Save EDS event for workflowInstanceId: {}", request.getWorkflowInstanceId());
        } catch(Exception ex) {
            log.error("Error while saving EDS event for workflowInstanceId: {}", request.getWorkflowInstanceId(), ex);
            throw new BusinessException(ex);
        }
    }

    @Override
    public CallbackTransactionDataResponseDTO saveTransactionStatus(CallbackTransactionDataRequestDTO request) {
        try {
            log.info("START - Save transaction status for workflowInstanceId: {}", request.getWorkflowInstanceId());

      TransactionDataETY savedEntity =
          transactionEventsRepo.saveEvent(
              request.getWorkflowInstanceId(),
              request.getInsertionDate(),
              request.getType(),
              request.getStatus(),
              null,
              null,
              null,
              request.getMessage());

            log.info("Transaction status saved successfully for workflowInstanceId: {}",
                    request.getWorkflowInstanceId());

            handleFinalStatusNotification(savedEntity);

            log.info("END - Save transaction status for workflowInstanceId: {}", request.getWorkflowInstanceId());

            return CallbackTransactionDataResponseDTO.builder()
                    .success(Boolean.TRUE)
                    .build();

        } catch (Exception ex) {
            log.error("Error while saving transaction status for workflowInstanceId: {}",
                    request.getWorkflowInstanceId(), ex);
            throw new BusinessException("Error while saving transaction status", ex);
        }
    }

    /**
     * Handle final status notification to Touchpoint Regionale.
     * Sends Kafka notification if the status is final, logs warning if notification
     * fails.
     *
     * @param savedEntity Transaction data entity
     */
    private void handleFinalStatusNotification(TransactionDataETY savedEntity) {
        if (!isFinalStatus(savedEntity.getType(), savedEntity.getStatus())) {
            log.debug("Status {} - {} is not final, skipping Touchpoint notification for workflowInstanceId: {}",
                    savedEntity.getType(), savedEntity.getStatus(), savedEntity.getWorkflowInstanceId());
            return;
        }

        try {
            sendFinalStatusKafkaMessage(savedEntity.getId(), savedEntity.getWorkflowInstanceId());
            log.info("Touchpoint notification sent successfully for workflowInstanceId: {} with final status: {} - {}",
                    savedEntity.getWorkflowInstanceId(), savedEntity.getType(), savedEntity.getStatus());
        } catch (Exception kafkaEx) {
            log.warn("Failed to send Touchpoint notification for workflowInstanceId: {}. Error: {}",
                    savedEntity.getWorkflowInstanceId(), kafkaEx.getMessage());
        }
    }

    /**
     * Handle final status notification to Touchpoint Regionale (overload for
     * JSON-based events).
     * Sends Kafka notification if the status is final, logs warning if notification
     * fails.
     *
     * @param workflowInstanceId Workflow instance ID
     * @param eventType          Event type
     * @param eventStatus        Event status
     */
    private void handleFinalStatusNotification(String workflowInstanceId, String eventType, String eventStatus) {
        if (!isFinalStatus(eventType, eventStatus)) {
            log.debug("Status {} - {} is not final, skipping Touchpoint notification for workflowInstanceId: {}",
                    eventType, eventStatus, workflowInstanceId);
            return;
        }

        try {
            // For JSON-based events, we use workflowInstanceId as both key and value
            sendFinalStatusKafkaMessage(workflowInstanceId, workflowInstanceId);
            log.info("Touchpoint notification sent successfully for workflowInstanceId: {} with final status: {} - {}",
                    workflowInstanceId, eventType, eventStatus);
        } catch (Exception kafkaEx) {
            log.warn("Failed to send Touchpoint notification for workflowInstanceId: {}. Error: {}",
                    workflowInstanceId, kafkaEx.getMessage());
        }
    }

    /**
     * Send notification to Touchpoint Regionale via Kafka.
     *
     * @param entityId           Entity ID
     * @param workflowInstanceId Workflow instance ID
     */
    private void sendFinalStatusKafkaMessage(String entityId, String workflowInstanceId) {
        try {
            kafkaTemplate.send(finalStateTopic, entityId, workflowInstanceId);
            log.debug("Kafka message sent to topic: {} for workflowInstanceId: {}", finalStateTopic,
                    workflowInstanceId);
        } catch (Exception ex) {
            log.error("Error sending Kafka notification to Touchpoint Regionale for workflowInstanceId: {}",
                    workflowInstanceId, ex);
            throw new BusinessException("Error sending Kafka notification", ex);
        }
    }

    /**
     * Check if the given type and status combination represents a final status.
     *
     * @param type   Event type
     * @param status Event status
     * @return true if final status, false otherwise
     */
    private boolean isFinalStatus(String type, String status) {
        if (type == null || status == null) {
            return false;
        }

        return ("SEND_TO_INI".equals(type) && "BLOCKING_ERROR".equals(status)) ||
                ("SEND_TO_UAR".equals(type) && "BLOCKING_ERROR".equals(status)) ||
                ("UAR_FINAL_STATUS".equals(type) && "SUCCESS".equals(status)) ||
                ("UAR_FINAL_STATUS".equals(type) && "BLOCKING_ERROR".equals(status));
    }
}
