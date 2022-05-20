package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.TransactionEventDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class TransactionEventsRepo extends AbstractMongoRepository<TransactionEventsETY, String>
		implements ITransactionEventsRepo {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -4017623557412046071L;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public TransactionEventsETY insert(final TransactionEventsETY ety) {
		return super.insert(ety);
	}

	@Override
	public TransactionEventsETY findByTransactionID(final String transactionID) {
		TransactionEventsETY output = null;
		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("transactionID").is(transactionID));
			output = mongoTemplate.findOne(query, TransactionEventsETY.class);
		} catch (Exception ex) {
			String error = "Error retrieving Validation Event by txID";
			log.error(error, ex);
			throw new BusinessException(error, ex);
		}
		return output;
	}

	@Override
	public void updateValidationEvent(String id, TransactionEventDTO event, Date eventDate) {

		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("_id").is(new ObjectId(id)));

			Update update = new Update();
			update.set("last_update", eventDate);
			update.set("last_event_type", event.getEventType());
			update.set("last_validation_result", event.getValidationResult());
			update.addToSet("events", event);

			mongoTemplate.updateFirst(query, update, TransactionEventsETY.class);

		} catch (Exception e) {
			String error = "Error updating Validation Event for element with id: " + id;
			log.error(error, e);
			throw new BusinessException(error, e);
		}

	}


	@Override
	public void updatePublicationEvent(String id, TransactionEventDTO event, Date eventDate) {

		try {
			Query query = new Query();
			query.addCriteria(Criteria.where("_id").is(new ObjectId(id)));

			Update update = new Update();
			update.set("last_update", eventDate);
			update.set("last_event_type", event.getEventType());
			update.set("last_publication_result", event.getPublicationResult());
			update.addToSet("events", event);

			mongoTemplate.updateFirst(query, update, TransactionEventsETY.class);

		} catch (Exception e) {
			String error = "Error updating Publication Event for element with id: " + id;
			log.error(error, e);
			throw new BusinessException(error, e);
		}

	}

	@Override
	public List<TransactionEventsETY> findAll() {
		return super.findAll();
	}

	@Override
	public List<TransactionEventsETY> findTransactions(ValidationResultEnum lastValidationResult,
    PublicationResultEnum lastPublicationResult, EventTypeEnum lastEventType, EventTypeEnum eventType,
    ValidationResultEnum eventValidationResult, PublicationResultEnum eventPublicationResult,
    String identificativoDoc, String identificativoPaziente, String identificativoSottomissione,
    Boolean forcePublish, String startDateStr, String endDateStr) {

		List<TransactionEventsETY> output = null;
		try {
			Query query = new Query();

			// --- lastValidationResult

			if (lastValidationResult != null) {
				query.addCriteria(Criteria.where("last_validation_result").is(lastValidationResult));
			}

			// --- lastValidationResult

			if (lastPublicationResult != null) {
				query.addCriteria(Criteria.where("last_publication_result").is(lastPublicationResult));
			}

			// --- lastEventType

			if (lastEventType != null) {
				query.addCriteria(Criteria.where("last_event_type").is(lastEventType));
			}

			// --- events array content

			if (eventType != null) {
				query.addCriteria(Criteria.where("events.eventType").is(eventType));
			}


			if (identificativoDoc != null && identificativoDoc.length() > 0) {
				query.addCriteria(Criteria.where("events.identificativoDoc").is(identificativoDoc));
			}

			if (identificativoPaziente != null && identificativoPaziente.length() > 0) {
				query.addCriteria(Criteria.where("events.identificativoPaziente").is(identificativoPaziente));
			}

			if (identificativoSottomissione != null && identificativoSottomissione.length() > 0) {
				query.addCriteria(Criteria.where("events.identificativoSottomissione").is(identificativoSottomissione));
			}

			if (forcePublish != null ) {
				if(forcePublish) {
					query.addCriteria(Criteria.where("events.forcePublish").is(true));
				} else {
					query.addCriteria(Criteria.where("events.forcePublish").is(false));
				}
			}

			if (eventValidationResult != null) {
				query.addCriteria(Criteria.where("events.validationResult").is(eventValidationResult));
			}

			if (eventPublicationResult != null) {
				query.addCriteria(Criteria.where("events.publicationResult").is(eventPublicationResult));
			}

			// --- dates

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			if (startDateStr != null && startDateStr.length() > 0 && (endDateStr == null || endDateStr.length() == 0)) {
				// startDate != null && endDate == null
				Date startDate = sdf.parse(startDateStr);
				query.addCriteria(Criteria.where("last_update").gte(startDate));
			} else if (startDateStr != null && startDateStr.length() > 0 && endDateStr != null
					&& endDateStr.length() > 0) {
				// startDate != null && endDate != null
				Date startDate = sdf.parse(startDateStr);
				Date endDate = sdf.parse(endDateStr);
				query.addCriteria(Criteria.where("last_update").gte(startDate).lte(endDate));
			} else if ((startDateStr == null || startDateStr.length() == 0) && endDateStr != null
					&& endDateStr.length() > 0) {
				// startDate == null && endDate != null
				Date endDate = sdf.parse(endDateStr);
				query.addCriteria(Criteria.where("last_update").lte(endDate));
			}

			output = mongoTemplate.find(query, TransactionEventsETY.class);
		} catch (Exception ex) {
			String error = "Error retrieving Validation Event with the provided filters";
			log.error(error, ex);
			throw new BusinessException(error, ex);
		}
		return output;
	}

}
