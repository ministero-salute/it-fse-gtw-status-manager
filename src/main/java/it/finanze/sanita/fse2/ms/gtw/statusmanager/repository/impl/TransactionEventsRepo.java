/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.ProfileUtility;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class TransactionEventsRepo implements ITransactionEventsRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -4017623557412046071L;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ProfileUtility profileUtility;

	@Override
	public void saveEvent(String workflowInstanceId, String json) {
		try {
			Document doc = Document.parse(json);
			
			String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			simpleDateFormat.setTimeZone(TimeZone.getDefault());
			Date eventDate = simpleDateFormat.parse(doc.getString("eventDate"));
			doc.put("eventDate", eventDate);
			doc.put("workflow_instance_id", workflowInstanceId);
			String collection = Constants.ComponentScan.Collections.TRANSACTION_DATA;
			if (profileUtility.isTestProfile()) {
				collection = Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.TRANSACTION_DATA;
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
			mongoTemplate.upsert(query, Update.fromDocument(doc, "_id"), collection);
			
		} catch(Exception ex){
			log.error("Error while save event : " , ex);
			throw new BusinessException("Error while save event : " , ex);
		}
	}
	 
}
