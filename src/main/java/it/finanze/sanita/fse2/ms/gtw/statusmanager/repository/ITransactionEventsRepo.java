/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Validation Event interface repository
 */
public interface ITransactionEventsRepo extends Serializable {
  
	void saveEvent(String json, String workflowInstanceId);

	int saveEventsFhir(List<String> wif, OffsetDateTime timestamp);
}
