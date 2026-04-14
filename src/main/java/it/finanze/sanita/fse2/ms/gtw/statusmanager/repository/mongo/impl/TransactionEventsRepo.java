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
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionDataETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.DateUtility;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
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
            mongo.upsert(query, Update.fromDocument(doc, "_id"), TransactionDataETY.class);
        } catch(Exception ex){
            log.error("Error while save event : " , ex);
            throw new BusinessException("Error while save event : " , ex);
        }
    }

	@Override
	public TransactionDataETY saveEvent(String workflowInstanceId, Date eventDate, String eventType, String eventStatus,
			String traceId, String issuer, String subject, String detail) {
		try {
			TransactionDataETY entity = new TransactionDataETY();
			entity.setWorkflowInstanceId(workflowInstanceId);
			entity.setDate(eventDate);
			entity.setType(eventType);
			entity.setStatus(eventStatus);

			if (traceId != null) {
				entity.setTraceId(traceId);
			}

			if (issuer != null) {
				if (configSRV.isCfOnIssuerNotAllowed()) {
					entity.setIssuer(clearIssuer(issuer));
				} else {
					entity.setIssuer(issuer);
				}
			}

			if (subject != null && !configSRV.isSubjectNotAllowed()) {
				entity.setSubject(subject);
			}

			if (detail != null) {
				entity.setDetail(detail);
			}

	        Date expiringDate = DateUtility.addDay(new Date(), configSRV.getExpirationDate());
	        entity.setExpiringDate(expiringDate);
	        return mongo.insert(entity);

		} catch (Exception ex) {
			log.error("Error while save event : ", ex);
			throw new BusinessException("Error while save event : ", ex);
		}
	}

    @Override
    public TransactionDataETY saveEdsEvent(String workflowInstanceId, Date date, String type, String status) {
        TransactionDataETY transactionDataETY = new TransactionDataETY();
        transactionDataETY.setWorkflowInstanceId(workflowInstanceId);
        transactionDataETY.setDate(date);
        transactionDataETY.setType(type);
        transactionDataETY.setStatus(status);
        return mongo.save(transactionDataETY);
    }

    @Override
	public int saveEventsFhir(List<String> wif, Date timestamp, Date expiration) throws OperationException {
		// Working var
		int insertions;
		// Convert each wif into fhir event
		// Using .parallel() to speed up the work
		List<TransactionDataETY> events = wif
			.stream()
			.parallel()
			.map(id -> TransactionDataETY.asSuccess(id, timestamp, expiration))
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

	@Override
	public List<TransactionDataETY> findPendingUarTransactions(Date thresholdDate, int maxResults) {
		/*
		 * Optimized MongoDB aggregation query that finds workflowInstanceId with:
		 * - At least one SEND_TO_UAR SUCCESS event older than threshold
		 * - NO UAR_FINAL_STATUS events
		 * - NO pullStatusOutcome="blocked" (already failed transactions)
		 * This is much more efficient than multiple queries + filtering in memory
		 */

		// Step 1: Match all events for potential workflow IDs
		org.springframework.data.mongodb.core.aggregation.MatchOperation matchAll = org.springframework.data.mongodb.core.aggregation.Aggregation
				.match(
						new Criteria().orOperator(
								// SEND_TO_UAR SUCCESS older than threshold WITHOUT pullStatusOutcome="blocked"
								Criteria.where(TransactionDataETY.FIELD_EVENT_TYPE).is("SEND_TO_UAR")
										.and(TransactionDataETY.FIELD_EVENT_STATUS).is("SUCCESS")
										.and(TransactionDataETY.FIELD_EVENT_DATE).lt(thresholdDate)
										.and(TransactionDataETY.FIELD_PULL_STATUS_OUTCOME).ne(TransactionDataETY.PULL_STATUS_BLOCKED),
								// OR any final status event
								Criteria.where(TransactionDataETY.FIELD_EVENT_TYPE).in(
										TransactionDataETY.FHIR_TYPE_UAR)));

		// Step 2: Group by workflowInstanceId and collect event types
		org.springframework.data.mongodb.core.aggregation.GroupOperation groupByWif = org.springframework.data.mongodb.core.aggregation.Aggregation
				.group(TransactionDataETY.FIELD_WIF)
				.first(TransactionDataETY.FIELD_WIF).as("workflowInstanceId")
				.first(TransactionDataETY.FIELD_EVENT_DATE).as("eventDate")
				.addToSet(TransactionDataETY.FIELD_EVENT_TYPE).as("eventTypes");

		// Step 3: Match only groups that have SEND_TO_UAR but NOT final status
		org.springframework.data.mongodb.core.aggregation.MatchOperation matchPending = org.springframework.data.mongodb.core.aggregation.Aggregation
				.match(
						Criteria.where("eventTypes").all("SEND_TO_UAR")
								.nin(TransactionDataETY.FHIR_TYPE_UAR));

		// Step 4: Project to return only needed fields
		org.springframework.data.mongodb.core.aggregation.ProjectionOperation project = org.springframework.data.mongodb.core.aggregation.Aggregation
				.project()
				.and("workflowInstanceId").as(TransactionDataETY.FIELD_WIF)
				.and("eventDate").as(TransactionDataETY.FIELD_EVENT_DATE);

		// Step 5: Limit results to prevent processing too many transactions at once
		org.springframework.data.mongodb.core.aggregation.LimitOperation limit = org.springframework.data.mongodb.core.aggregation.Aggregation
				.limit(maxResults);

		// Execute aggregation
		org.springframework.data.mongodb.core.aggregation.Aggregation aggregation = org.springframework.data.mongodb.core.aggregation.Aggregation
				.newAggregation(
						matchAll,
						groupByWif,
						matchPending,
						project,
						limit)
                .withOptions(Aggregation.newAggregationOptions().allowDiskUse(true).build());

		org.springframework.data.mongodb.core.aggregation.AggregationResults<TransactionDataETY> results = mongo
				.aggregate(aggregation, TransactionDataETY.class, TransactionDataETY.class);

		return results.getMappedResults();
	}

	@Override
	public boolean updatePullStatusOutcome(String workflowInstanceId, String outcome) {
		try {
			// Query to find the SEND_TO_UAR SUCCESS event for this workflow
			Query query = new Query();
			query.addCriteria(
					Criteria.where(TransactionDataETY.FIELD_WIF).is(workflowInstanceId)
							.and(TransactionDataETY.FIELD_EVENT_TYPE).is("SEND_TO_UAR")
							.and(TransactionDataETY.FIELD_EVENT_STATUS).is("SUCCESS"));

			// Update to set the pullStatusOutcome field
			Update update = new Update();
			update.set(TransactionDataETY.FIELD_PULL_STATUS_OUTCOME, outcome);

			// Execute update
			var result = mongo.updateFirst(query, update, TransactionDataETY.class);

			boolean success = result.getModifiedCount() > 0;
			if (success) {
				log.info("[REPO] Updated pullStatusOutcome to '{}' for workflowInstanceId: {}",
						outcome, workflowInstanceId);
			} else {
				log.warn("[REPO] No SEND_TO_UAR event found to update for workflowInstanceId: {}",
						workflowInstanceId);
			}

			return success;

		} catch (MongoException ex) {
			log.error("[REPO] Error updating pullStatusOutcome for workflowInstanceId: {}",
					workflowInstanceId, ex);
			return false;
		}
	}

}

