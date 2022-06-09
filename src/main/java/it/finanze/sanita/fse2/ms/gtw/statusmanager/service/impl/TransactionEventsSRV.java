package it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import lombok.extern.slf4j.Slf4j;

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
    private ITransactionEventsRepo transactionEventsRepo;

	@Override
    public void saveEvent(final String workflowInstanceId , final String json) {
    	try {
    		transactionEventsRepo.saveEvent(workflowInstanceId,json);
    	} catch(Exception ex) {
    		log.error("Errore while save event : " , ex);
    		throw new BusinessException(ex);
    	}
    }
 

}
