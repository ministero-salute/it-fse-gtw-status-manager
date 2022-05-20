package it.finanze.sanita.fse2.ms.gtw.statusmanager.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.TransactionEventDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;

/**
 * Validation Event interface repository
 */
public interface ITransactionEventsRepo extends Serializable {

	/**
	 * Insert a Validation Event on database.
	 * 
	 * @param ety Validation Event to insert.
	 * @return Validation Event inserted.
	 */
	TransactionEventsETY insert(TransactionEventsETY ety);

	/**
	 * Return a Validation Event by transaction ID
	 * 
	 * @param transactionID
	 * @return
	 */
	TransactionEventsETY findByTransactionID(String transactionID);

	/**
	 * Update Transaction Event by adding a new validation event to the list
	 * 
	 * @param mongo     id
	 * @param event
	 * @param eventDate
	 */
	void updateValidationEvent(String id, TransactionEventDTO event, Date eventDate);

	/**
	 * Update Publication Event by adding a new publication event to the list
	 * 
	 * @param mongo     id
	 * @param event
	 * @param eventDate
	 */
	void updatePublicationEvent(String id, TransactionEventDTO event, Date eventDate);

	/**
	 * Return a list of all Validation Events.
	 * 
	 * @return List of all Validation Events.
	 */
	List<TransactionEventsETY> findAll();

	/**
	 * Return a list of validation event transactions that matches the provided
	 * fielters
	 * 
	 * @param lastValidationResult
	 * @param lastPublicationResult
	 * @param lastEventType
	 * @param eventType
	 * @param eventValidationResult
	 * @param eventPublicationResult
	 * @param identificativoDoc
	 * @param identificativoPaziente
	 * @param identificativoSottomissione
	 * @param forcePublish
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	List<TransactionEventsETY> findTransactions(ValidationResultEnum lastValidationResult,
    PublicationResultEnum lastPublicationResult, EventTypeEnum lastEventType, EventTypeEnum eventType,
    ValidationResultEnum eventValidationResult, PublicationResultEnum eventPublicationResult,
    String identificativoDoc, String identificativoPaziente, String identificativoSottomissione,
    Boolean forcePublish, String startDate, String endDate);

}
