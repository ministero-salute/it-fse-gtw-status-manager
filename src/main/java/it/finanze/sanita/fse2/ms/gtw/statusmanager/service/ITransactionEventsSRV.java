/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.service;

import java.io.Serializable;

public interface ITransactionEventsSRV extends Serializable {

	void saveEvent(String workflowInstanceId , String json);

}
