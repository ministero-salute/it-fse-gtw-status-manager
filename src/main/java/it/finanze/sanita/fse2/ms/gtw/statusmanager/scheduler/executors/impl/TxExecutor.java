/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IProcessorClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.DeleteTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.base.IActionStepEDS;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.base.LExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.impl.ProcessorClient.CHUNK_LIMIT;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes.*;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.DateUtility.getCurrentTime;
import static java.lang.String.format;

@Slf4j
@Component
public class TxExecutor extends LExecutor {

    public static final String TITLE = "TX";

    @Autowired
    private IProcessorClient processor;

    @Autowired
    private ITransactionEventsRepo transaction;

    private OffsetDateTime timestamp;

    private GetTxResDTO changeset;

    private long processed;
    private long deleted;

    @Override
    protected ActionRes onReset() {
        this.changeset = null;
        this.timestamp = null;
        this.processed = 0;
        this.deleted = 0;
        return OK;
    }

    @Override
    protected LinkedHashMap<String, IActionStepEDS> getSteps() {
        // Working var
        LinkedHashMap<String, IActionStepEDS> steps = new LinkedHashMap<>();
        // Append steps
        steps.put("RESET", this::onReset);
        steps.put("INIT", this::onInit);
        steps.put("VERIFY", this::onVerify);
        steps.put("PROCESS", this::onProcess);
        steps.put("DELETE", this::onDelete);
        // Return new state
        return steps;
    }

    protected ActionRes onInit() {
        // Working var
        ActionRes res = KO;
        // Define current time
        this.timestamp = getCurrentTime();
        // Display timestamp
        log.info("[{}] - Current timestamp: {}", TITLE, timestamp);
        try {
            // Execute request
            this.changeset = processor.getTransactions(timestamp, 0, CHUNK_LIMIT);
            // Flag it
            res = OK;
        }catch (Exception ex) {
            log.error(
                format("[%s] Unable to retrieve transaction changeset", TITLE),
                ex
            );
        }
        return res;
    }

    protected ActionRes onVerify() {
        return this.changeset.getWif().isEmpty() ? EMPTY : OK;
    }

    protected ActionRes onProcess() {
        // Working var
        ActionRes res = KO;
        try {
            // Save request size
            this.processed = changeset.getWif().size();
            // Process current one
            for (String wif : changeset.getWif()) {
                transaction.saveEventFhir(wif, timestamp);
            }
            // Iterate until data is exhausted given a previous request
            while (changeset.getLinks().getNext() != null) {
                // Next request
                changeset = processor.getTransactions(changeset.getLinks().getNext());
                // Save request size
                processed += changeset.getWif().size();
                // Save transaction
                for (String wif : changeset.getWif()) {
                    transaction.saveEventFhir(wif, timestamp);
                }
            }
            // Flag it
            res = OK;
            // Display
            log.debug("[{}] - Updated transactions: {}", TITLE, processed);
        }catch (Exception ex){
            log.error(
                format("[%s] Unable to update transactions", TITLE),
                ex
            );
        }
        return res;
    }

    protected ActionRes onDelete() {
        // Working var
        ActionRes res = KO;
        try {
            // Execute request
            DeleteTxResDTO tx = processor.deleteTransactions(timestamp);
            // Retrieve value
            deleted = tx.getDeletedTransactions();
            // Flag it
            res = OK;
            // Log it
            log.debug("[{}] - Deleted transactions: {}", TITLE, deleted);
        }catch (Exception ex) {
            log.error(
                format("[%s] Unable to delete transactions", TITLE),
                ex
            );
        }
        return res;
    }
}
