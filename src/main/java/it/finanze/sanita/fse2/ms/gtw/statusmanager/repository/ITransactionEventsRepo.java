/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Validation Event interface repository
 */
public interface ITransactionEventsRepo extends Serializable {
  
	void saveEvent(String json, String workflowInstanceId);

	void saveEvent(String wif, String type, String outcome, OffsetDateTime timestamp);
}
