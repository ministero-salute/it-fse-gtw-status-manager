/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.OperationException;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Validation Event interface repository
 */
public interface ITransactionEventsRepo extends Serializable {
  
	void saveEvent(String json, String workflowInstanceId);

	int saveEventsFhir(List<String> wif, Date timestamp, Date expiration)throws OperationException;
}
