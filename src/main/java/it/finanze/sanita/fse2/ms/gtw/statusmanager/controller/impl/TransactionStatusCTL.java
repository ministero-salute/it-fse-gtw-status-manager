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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.ITransactionStatusCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller implementation for transaction status operations.
 */
@RestController
@Slf4j
public class TransactionStatusCTL implements ITransactionStatusCTL {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String EVENT_TYPE = "eventType";
    private static final String EVENT_DATE = "eventDate";
    private static final String EVENT_STATUS = "eventStatus";
    private static final String MESSAGE = "message";

    @Autowired
    private ITransactionEventsSRV transactionEventsSRV;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public CallbackTransactionDataResponseDTO saveTransactionStatus(@Valid CallbackTransactionDataRequestDTO request) {
        log.info("[START] saveTransactionStatus() - workflowInstanceId: {}, status: {}",
            request.getWorkflowInstanceId(), request.getStatus());

        try {
            String workflowInstanceId = request.getWorkflowInstanceId();
            String json = serializeToJson(request);
            transactionEventsSRV.saveEvent(workflowInstanceId, json);

            log.info("[END] saveTransactionStatus() - workflowInstanceId: {}, success: true",
                    workflowInstanceId);

            return CallbackTransactionDataResponseDTO.builder()
                    .success(Boolean.TRUE)
                    .build();

        } catch (BusinessException ex) {
            log.error("Business error in saveTransactionStatus for workflowInstanceId: {}",
                    request.getWorkflowInstanceId(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in saveTransactionStatus for workflowInstanceId: {}",
                    request.getWorkflowInstanceId(), ex);
            throw new BusinessException("Error saving transaction status", ex);
        }
    }

    private String serializeToJson(CallbackTransactionDataRequestDTO request) throws JsonProcessingException {
        Map<String, Object> jsonMap = new HashMap<>();

        jsonMap.put(EVENT_TYPE, request.getType());
        jsonMap.put(EVENT_DATE, formatDate(request.getInsertionDate()));
        jsonMap.put(EVENT_STATUS, request.getStatus());
        if (request.getMessage() != null) {
            jsonMap.put(MESSAGE, request.getMessage());
        }

        String json = objectMapper.writeValueAsString(jsonMap);
        log.debug("Serialized JSON for workflowInstanceId {}: {}",
                request.getWorkflowInstanceId(), json);

        return json;
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

}
