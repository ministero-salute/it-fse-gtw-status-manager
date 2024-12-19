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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.impl;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.OperationException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.FhirEvent;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.DateUtility;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants.Fields.*;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants.Logs.ERR_REP_FHIR_EVENTS;

@Slf4j
@Repository
public class TransactionEventsRepo implements ITransactionEventsRepo {

	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	@Autowired
	private MongoTemplate mongo;
	
	@Autowired
	private IConfigSRV configSRV;

	@Override
	public void saveEvent(String workflowInstanceId, String json) {
		try {
			Document doc = Document.parse(json);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PATTERN);
			simpleDateFormat.setTimeZone(TimeZone.getDefault());
			Date eventDate = simpleDateFormat.parse(doc.getString(EVENT_DATE));
			doc.put(EVENT_DATE, eventDate);
			doc.put(WORKFLOW_INSTANCE_ID, workflowInstanceId);
			String eventType = doc.getString(EVENT_TYPE);
			String eventStatus = doc.getString(EVENT_STATUS);
			Query query = new Query();
			if(!"UNKNOWN_WORKFLOW_ID".equals(workflowInstanceId)) {
				query.addCriteria(Criteria.where(WORKFLOW_INSTANCE_ID).is(workflowInstanceId).
						and(EVENT_TYPE).is(eventType).and(EVENT_STATUS).is(eventStatus));
			} else {
				query.addCriteria(Criteria.where(TRACE_ID).is(doc.getString(TRACE_ID)).
						and(EVENT_TYPE).is(eventType).and(EVENT_STATUS).is(eventStatus));
			}
			Date expiringDate = DateUtility.addDay(new Date(), configSRV.getExpirationDate());
			doc.put(EXPIRING_DATE, expiringDate);
			clearIssuerObject(doc);
			clearSubjectObject(doc);
			mongo.upsert(query, Update.fromDocument(doc, "_id"), FhirEvent.class);
		} catch(Exception ex){
			log.error("Error while save event : " , ex);
			throw new BusinessException("Error while save event : " , ex);
		}
	}

	@Override
	public int saveEventsFhir(List<String> wif, Date timestamp, Date expiration) throws OperationException {
		// Working var
		int insertions;
		// Convert each wif into fhir event
		// Using .parallel() to speed up the work
		List<FhirEvent> events = wif
			.stream()
			.parallel()
			.map(id -> FhirEvent.asSuccess(id, timestamp, expiration))
			.collect(Collectors.toList());
		// Insert
		try {
			insertions = mongo.insertAll(events).size();
		} catch (MongoException ex) {
			throw new OperationException(ERR_REP_FHIR_EVENTS, ex);
		}
		return insertions;
	}


	private void clearSubjectObject(Document doc) {
		if (doc.containsKey(EVENT_SUBJECT) && configSRV.isSubjectNotAllowed()) {
			doc.remove(EVENT_SUBJECT);
		}
	}

	private void clearIssuerObject(Document doc) {
		if(doc.containsKey(EVENT_ISSUER) && configSRV.isCfOnIssuerNotAllowed()) {
			doc.replace(EVENT_ISSUER, clearIssuer(doc.getString(EVENT_ISSUER)));
		}
	}

	private String clearIssuer(String issuer) {
		return issuer.contains("#") ? issuer.split("#")[0] : issuer;
	}

}
