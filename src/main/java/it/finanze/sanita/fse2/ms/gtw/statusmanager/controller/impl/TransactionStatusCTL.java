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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.ITransactionStatusCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller implementation for transaction status operations.
 */
@RestController
@Slf4j
public class TransactionStatusCTL implements ITransactionStatusCTL {

    @Autowired
    private ITransactionEventsSRV transactionEventsSRV;

    @Override
    public CallbackTransactionDataResponseDTO saveTransactionStatus(@Valid CallbackTransactionDataRequestDTO request) {
        log.info("[START] saveTransactionStatus() - workflowInstanceId: {}, status: {}",
            request.getWorkflowInstanceId(), request.getStatus());
        CallbackTransactionDataResponseDTO response = transactionEventsSRV.saveTransactionStatus(request);
        log.info("[END] saveTransactionStatus() - workflowInstanceId: {}, success: {}",
            request.getWorkflowInstanceId(), response.getSuccess());
        
        return response;
    }

}
