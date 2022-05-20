package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.IErrorTransactionInspectionCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.TransactionDetailResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.TransactionsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.EntityNotFoundException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Error Transaction Inspection controller.
 */
@RestController
@Slf4j
public class ErrorTransactionInspectionCTL extends AbstractCTL implements IErrorTransactionInspectionCTL {

    @Autowired
    private ITransactionEventsSRV transactionEventSRV;

    @Override
    public TransactionDetailResponseDTO getTransactionDetail(final String transactionID, final HttpServletRequest request) {

        if (StringUtility.isNullOrEmpty(transactionID)) {
            throw new ValidationException("Valorizzare il parametro Transaction ID");
        }

        TransactionEventsETY transaction = null;
        try {
            transaction = transactionEventSRV.getTransactionEventByTxID(transactionID, true);
        } catch (Exception e) {
            log.error("error");
        }

        if (transaction == null) {
            throw new EntityNotFoundException(
                    "Attenzione nessuna transazione in errore presente con transaction id: " + transactionID);
        }

        return new TransactionDetailResponseDTO(getLogTraceInfo(), transaction);
    }

    @Override
    public TransactionsResponseDTO findErrorTransaction(EventTypeEnum lastEventType, EventTypeEnum eventType,
            String identificativoDoc, String identificativoPaziente, String identificativoSottomissione,
            Boolean forcePublish, String startDate, String endDate, HttpServletRequest request) {

        if (startDate != null) {
            StringUtility.dateFormatValid(startDate);
        }
        if (endDate != null) {
            StringUtility.dateFormatValid(endDate);
        }

        List<TransactionEventsETY> transactions = new ArrayList<>();

        try {
            transactions = transactionEventSRV.findTransactions(null, null, lastEventType, eventType, null, null, identificativoDoc, identificativoPaziente, identificativoSottomissione, forcePublish, startDate, endDate, true);
        } catch (Exception e) {
            log.error("Error retrieving validation event transactions in error status with filters");
            throw new BusinessException("Error retrieving validation event transactions in error status with filters",
                    e);
        }

        return new TransactionsResponseDTO(getLogTraceInfo(), transactions);
    }

}
