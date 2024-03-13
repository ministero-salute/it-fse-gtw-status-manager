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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import lombok.extern.slf4j.Slf4j;

/**
 * Transaction Event service.
 */
@Service
@Slf4j
public class TransactionEventsSRV implements ITransactionEventsSRV {

	@Autowired
    private transient ITransactionEventsRepo transactionEventsRepo;

	@Override
    public void saveEvent(final String workflowInstanceId , final String json, final String traceId) {
    	try {
    		log.info("START - Save event");
    		transactionEventsRepo.saveEvent(workflowInstanceId,json,traceId);
    		log.info("END - Save event");
    	} catch(Exception ex) {
    		log.error("Errore while save event : " , ex);
    		throw new BusinessException(ex);
    	}
    }
 

}
