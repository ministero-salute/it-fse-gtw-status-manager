/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.impl;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.OperationException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.FhirEvent;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants.Logs.ERR_REP_FHIR_EVENTS;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Slf4j
@Repository
public class TransactionEventsRepo implements ITransactionEventsRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -4017623557412046071L;

	private static final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	
	@Autowired
	private MongoTemplate mongo;

	@Autowired
	private ProfileUtility profileUtility;

	@Override
	public void saveEvent(String workflowInstanceId, String json) {
		try {
			Document doc = Document.parse(json);
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			simpleDateFormat.setTimeZone(TimeZone.getDefault());
			Date eventDate = simpleDateFormat.parse(doc.getString("eventDate"));
			doc.put("eventDate", eventDate);
			doc.put("workflow_instance_id", workflowInstanceId);
			String collection = Constants.Collections.TRANSACTION_DATA;
			if (profileUtility.isTestProfile()) {
				collection = Constants.Profile.TEST_PREFIX + Constants.Collections.TRANSACTION_DATA;
			}
			
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
			mongo.upsert(query, Update.fromDocument(doc, "_id"), collection);
			
		} catch(Exception ex){
			log.error("Error while save event : " , ex);
			throw new BusinessException("Error while save event : " , ex);
		}
	}

	@Override
	public int saveEventsFhir(List<String> wif, OffsetDateTime timestamp) throws OperationException {
		// Working var
		int insertions;
		String time = ISO_DATE_TIME.format(timestamp);
		// Convert each wif into fhir event
		// Using .parallel() to speed up the work
		List<FhirEvent> events = wif
			.stream()
			.parallel()
			.map(id -> FhirEvent.asSuccess(id, time))
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
