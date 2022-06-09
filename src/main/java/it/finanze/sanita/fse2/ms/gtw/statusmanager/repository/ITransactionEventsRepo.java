package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository;

import java.io.Serializable;

/**
 * Validation Event interface repository
 */
public interface ITransactionEventsRepo extends Serializable {
  
	void saveEvent(String json, String workflowInstanceId);
	
}
