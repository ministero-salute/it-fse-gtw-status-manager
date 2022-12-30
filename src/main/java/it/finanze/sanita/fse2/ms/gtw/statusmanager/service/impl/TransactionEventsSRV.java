/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Transaction Event service.
 */
@Service
@Slf4j
public class TransactionEventsSRV extends AbstractService implements ITransactionEventsSRV {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 8384735139163560923L;
	
	@Autowired
    private transient ITransactionEventsRepo transactionEventsRepo;

	@Override
    public void saveEvent(final String workflowInstanceId , final String json) {
    	try {
    		log.info("START - Save event");
    		transactionEventsRepo.saveEvent(workflowInstanceId,json);
    		log.info("END - Save event");
    	} catch(Exception ex) {
    		log.error("Errore while save event : " , ex);
    		throw new BusinessException(ex);
    	}
    }
 

}
