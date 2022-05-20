package it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.PublicationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.TransactionEventDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ValidationCDAInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Transaction Event service.
 */
@Service
@Slf4j
public class TransactionEventsSRV extends AbstractService implements ITransactionEventsSRV {

    @Autowired
    private ITransactionEventsRepo transactionEventsRepo;

    @Override
    public void saveValidationEvent(final ValidationCDAInfoDTO validationInfo, final ValidationResultEnum validationResult, final boolean isHistoricalDoc, final boolean isTSFeeding) {

        try {

            EventTypeEnum eventType = null;
            if (isHistoricalDoc) {
                eventType = EventTypeEnum.HISTORICAL_DOC_VALIDATION;
            } else if(isTSFeeding) {
                eventType = EventTypeEnum.TS_DOC_VALIDATION;
            } else {
                eventType = EventTypeEnum.VALIDATION;
            }

            TransactionEventsETY transactionEvent = transactionEventsRepo.findByTransactionID(validationInfo.getTransactionID());

            if (transactionEvent == null) {

                Date eventDate = new Date();

                transactionEvent = new TransactionEventsETY();
                transactionEvent.setTransactionID(validationInfo.getTransactionID());
                transactionEvent.setLastUpdate(eventDate);
                transactionEvent.setLastValidationResult(validationResult);
                transactionEvent.setLastEventType(eventType);

                TransactionEventDTO eventInfo = TransactionEventDTO.builder()
                        .date(eventDate).eventType(eventType)
                        .identificativoDoc(validationInfo.getIdentificativoDoc())
                        .identificativoPaziente(validationInfo.getIdentificativoPaziente())
                        .identificativoSottomissione(validationInfo.getIdentificativoSottomissione())
                        .validationResult(validationResult).build();

                List<TransactionEventDTO> events = new ArrayList<>();
                events.add(eventInfo);
                transactionEvent.setEvents(events);

                transactionEventsRepo.insert(transactionEvent);

                log.info("Saved new Validation event with transaction ID: " + validationInfo.getTransactionID());

            } else {

                Date eventDate = new Date();

                TransactionEventDTO eventInfo = TransactionEventDTO.builder()
                        .date(eventDate).eventType(eventType)
                        .identificativoDoc(validationInfo.getIdentificativoDoc())
                        .identificativoPaziente(validationInfo.getIdentificativoPaziente())
                        .identificativoSottomissione(validationInfo.getIdentificativoSottomissione())
                        .validationResult(validationResult).build();

                transactionEventsRepo.updateValidationEvent(transactionEvent.getId(), eventInfo, eventDate);

                log.info("Updated Validation event with transaction ID: " + validationInfo.getTransactionID());

            }

        } catch (Exception e) {
            String error = "Error saving Validation Event with transaction ID " + validationInfo.getTransactionID();
            log.error(error, e);
            throw new BusinessException(error, e);
        }

    }

    @Override
    public void savePublicationEvent(final PublicationInfoDTO publicationInfo, final PublicationResultEnum publicationResult, final boolean isHistoricalDoc, final boolean isTSFeeding) {
        
        try {

            EventTypeEnum eventType = null;
            if (isHistoricalDoc) {
                eventType = EventTypeEnum.HISTORICAL_DOC_PUBLICATION;
            } else if(isTSFeeding) {
                eventType = EventTypeEnum.TS_DOC_PUBLICATION;
            } else {
                eventType = EventTypeEnum.PUBLICATION;
            }

            TransactionEventsETY transactionEvent = transactionEventsRepo.findByTransactionID(publicationInfo.getTransactionID());

            if (transactionEvent == null) {

                Date eventDate = new Date();

                transactionEvent = new TransactionEventsETY();
                transactionEvent.setTransactionID(publicationInfo.getTransactionID());
                transactionEvent.setLastUpdate(eventDate);
                transactionEvent.setLastPublicationResult(publicationResult);
                transactionEvent.setLastEventType(eventType);
                

                TransactionEventDTO eventInfo = TransactionEventDTO.builder()
                        .date(eventDate).eventType(eventType)
                        .identificativoDoc(publicationInfo.getIdentificativoDoc())
                        .identificativoPaziente(publicationInfo.getIdentificativoPaziente())
                        .identificativoSottomissione(publicationInfo.getIdentificativoSottomissione())
                        .forcePublish(publicationInfo.getForcePublish())
                        .publicationResult(publicationResult).build();

                List<TransactionEventDTO> events = new ArrayList<>();
                events.add(eventInfo);
                transactionEvent.setEvents(events);

                transactionEventsRepo.insert(transactionEvent);

                log.info("Saved new Publication event with transaction ID: " + publicationInfo.getTransactionID());

            } else {

                Date eventDate = new Date();

                TransactionEventDTO eventInfo = TransactionEventDTO.builder()
                        .date(eventDate).eventType(eventType)
                        .identificativoDoc(publicationInfo.getIdentificativoDoc())
                        .identificativoPaziente(publicationInfo.getIdentificativoPaziente())
                        .identificativoSottomissione(publicationInfo.getIdentificativoSottomissione())
                        .forcePublish(publicationInfo.getForcePublish())
                        .publicationResult(publicationResult).build();

                transactionEventsRepo.updatePublicationEvent(transactionEvent.getId(), eventInfo, eventDate);

                log.info("Updated Publication event with transaction ID: " + publicationInfo.getTransactionID());

            }

            
        } catch (Exception e) {
            String error = "Error saving Publication Event with transaction ID " + publicationInfo.getTransactionID();
            log.error(error, e);
            throw new BusinessException(error, e);
        }
        
    }


    @Override
    public TransactionEventsETY getTransactionEventByTxID(final String transactionID, final Boolean errorOnly) {

        if (errorOnly) {
            TransactionEventsETY transaction = transactionEventsRepo.findByTransactionID(transactionID);
            return removeOkEvents(transaction);
        } else {
            return transactionEventsRepo.findByTransactionID(transactionID);
        }

    }

    @Override
    public List<TransactionEventsETY> findTransactions(ValidationResultEnum lastValidationResult,
    PublicationResultEnum lastPublicationResult, EventTypeEnum lastEventType, EventTypeEnum eventType,
    ValidationResultEnum eventValidationResult, PublicationResultEnum eventPublicationResult,
    String identificativoDoc, String identificativoPaziente, String identificativoSottomissione,
    Boolean forcePublish, String startDate, String endDate, Boolean errorOnly) {

        if (errorOnly) {
            List<TransactionEventsETY> transactions = transactionEventsRepo.findTransactions(null, null, lastEventType, eventType, null, null, identificativoDoc, identificativoPaziente, identificativoSottomissione, forcePublish, startDate, endDate);
            

            for (TransactionEventsETY transaction : transactions) {
                transaction = removeOkEvents(transaction);
            }

            transactions.removeIf(t -> t== null || t.getEvents() == null || t.getEvents().isEmpty());

            return transactions;
        } else {
            return transactionEventsRepo.findTransactions(lastValidationResult, lastPublicationResult, lastEventType,
                    eventType, eventValidationResult, eventPublicationResult, identificativoDoc,
                    identificativoPaziente, identificativoSottomissione, forcePublish, startDate, endDate);
        }
    }

    private TransactionEventsETY removeOkEvents(TransactionEventsETY transaction) {

        int initialEventSize = transaction.getEvents().size();

        transaction.getEvents()
                .removeIf(e -> ValidationResultEnum.OK.equals(e.getValidationResult())
                        || PublicationResultEnum.OK.equals(e.getPublicationResult())
                        || PublicationResultEnum.OK_FORCED.equals(e.getPublicationResult()));

        // if OK events have been removed return entity
        if (transaction != null && transaction.getEvents() != null &&
         !transaction.getEvents().isEmpty() && initialEventSize >= transaction.getEvents().size()) {
            return transaction;
        } else {
            return null;
        }

    }


}
