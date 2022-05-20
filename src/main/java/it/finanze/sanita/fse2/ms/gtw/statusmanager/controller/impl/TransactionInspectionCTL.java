package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.ITransactionInspectionCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.TransactionDetailResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.TransactionsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.EntityNotFoundException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Transaction Inspection controller.
 */
@RestController
@Slf4j
public class TransactionInspectionCTL extends AbstractCTL implements ITransactionInspectionCTL {

    @Autowired
    private ITransactionEventsSRV transactionEventSRV;

    @Override
    public TransactionDetailResponseDTO getTransactionDetail(String transactionID, HttpServletRequest request) {

        if (StringUtility.isNullOrEmpty(transactionID)) {
            throw new ValidationException("Valorizzare il parametro Transaction ID");
        }

        TransactionEventsETY transaction = null;
        try {
            transaction = transactionEventSRV.getTransactionEventByTxID(transactionID, false);
        } catch (Exception e) {
            log.error("Error retrieving validation event by transaction ID");
            throw new BusinessException("Error retrieving validation event by transaction ID", e);
        }

        if (transaction == null) {
            throw new EntityNotFoundException("Attenzione nessuna transazione presente con transaction id: " + transactionID);
        }

        return new TransactionDetailResponseDTO(getLogTraceInfo(), transaction);
    }

    @Override
    public TransactionsResponseDTO findTransaction(ValidationResultEnum lastValidationResult,
    PublicationResultEnum lastPublicationResult, EventTypeEnum lastEventType, EventTypeEnum eventType,
    ValidationResultEnum eventValidationResult, PublicationResultEnum eventPublicationResult,
    String identificativoDoc, String identificativoPaziente, String identificativoSottomissione,
    Boolean forcePublish, String startDate, String endDate, HttpServletRequest request) {

        if(startDate!=null){
            StringUtility.dateFormatValid(startDate);
        }
        if(endDate!=null){
            StringUtility.dateFormatValid(endDate);
        }

        List<TransactionEventsETY> transactions = new ArrayList<>();

        try {
            transactions = transactionEventSRV.findTransactions(lastValidationResult, lastPublicationResult,
                    lastEventType, eventType, eventValidationResult, eventPublicationResult, identificativoDoc,
                    identificativoPaziente, identificativoSottomissione, forcePublish, startDate, endDate, false);
        } catch (Exception e) {
            log.error("Error retrieving validation event transactions with filters");
            throw new BusinessException("Error retrieving validation event transactions with filters", e);
        }

        return new TransactionsResponseDTO(getLogTraceInfo(), transactions);
    }

}
