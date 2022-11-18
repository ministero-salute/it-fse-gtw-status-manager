/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
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

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.springframework.data.mongodb.core.BulkOperations.BulkMode.UNORDERED;

@Slf4j
@Repository
public class TransactionEventsRepo implements ITransactionEventsRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -4017623557412046071L;

	private static final String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	private static final String FHIR_OUTCOME = "SUCCESS";
	private static final String FHIR_TYPE = "FHIR_PROCESSING";
	
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
	public int saveEventsFhir(List<String> wif, OffsetDateTime timestamp) {
		// Create bulking request
		BulkOperations ops = mongo.bulkOps(
			UNORDERED,
			Document.class,
			getCollectionNaming()
		);
		// Iterate and add on req
		for (String id : wif) {
			// Create document
			Document doc = new Document();
			// Update field
			doc.put("eventDate", ISO_DATE_TIME.format(timestamp));
			doc.put("workflow_instance_id", id);
			doc.put("eventType", FHIR_TYPE);
			doc.put("eventStatus", FHIR_OUTCOME);
			// Add
			ops.insert(doc);
		}
		// Insert
		return ops.execute().getInsertedCount();
	}

	public String getCollectionNaming() {
		String collection = Constants.Collections.TRANSACTION_DATA;
		if (profileUtility.isTestProfile()) {
			collection = Constants.Profile.TEST_PREFIX + Constants.Collections.TRANSACTION_DATA;
		}
		return collection;
	}

}
