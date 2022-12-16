/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
			Date eventDate = simpleDateFormat.parse(doc.getString("eventDate"));
			doc.put("eventDate", eventDate);
			doc.put("workflow_instance_id", workflowInstanceId);
			String eventType = doc.getString("eventType");
			String eventStatus = doc.getString("eventStatus");
			Query query = new Query();
			if(!"UNKNOWN_WORKFLOW_ID".equals(workflowInstanceId)) {
				query.addCriteria(Criteria.where("workflow_instance_id").is(workflowInstanceId).
						and("eventType").is(eventType).and("eventStatus").is(eventStatus));
			} else {
				query.addCriteria(Criteria.where("traceId").is(doc.getString("traceId")).
						and("eventType").is(eventType).and("eventStatus").is(eventStatus));
			}
			Date expiringDate = DateUtility.addDay(new Date(), configSRV.getExpirationDate());
			doc.put("expiring_date", expiringDate);
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

}
