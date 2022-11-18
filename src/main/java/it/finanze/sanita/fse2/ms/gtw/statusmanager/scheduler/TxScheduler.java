/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IProcessorClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.ITransactionEventsRepo;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.AbstractMap.SimpleImmutableEntry;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.impl.ProcessorClient.CHUNK_LIMIT;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.DateUtility.getCurrentTime;

@Slf4j
@Component
public class TxScheduler {

    @Autowired
    private IProcessorClient processor;

    @Autowired
    private ITransactionEventsRepo transaction;

    @Scheduled(cron = "${scheduler.tx-scheduler}")
    @SchedulerLock(name = "invokeTxScheduler" , lockAtMostFor = "60m")
    public void action() {
        log.info("[TX] - Starting scheduled updating process");
        // Start
        SimpleImmutableEntry<OffsetDateTime, GetTxResDTO> init = onInit();
        // Display timestamp
        log.info("[TX] - Current timestamp: {}", init.getKey());
        // Check if skipping
        if(!init.getValue().getWif().isEmpty()) {
            // Process data
            log.info("[TX] - Updated transactions: {}", onProcess(init.getKey(), init.getValue()));
            // Delete data
            log.info("[TX] - Deleted transactions: {}", onDelete(init.getKey()));
        }else {
            log.info("[TX] - No transactions to process");
        }
        log.info("[TX] Updating process completed");
    }

    private SimpleImmutableEntry<OffsetDateTime, GetTxResDTO> onInit() {
        // Define current time
        OffsetDateTime timestamp = getCurrentTime();
        // Execute request
        GetTxResDTO tx = processor.getTransactions(timestamp, 0, CHUNK_LIMIT);
        // Return as a pair for further elaboration
        return new SimpleImmutableEntry<>(timestamp, tx);
    }

    private long onProcess(OffsetDateTime timestamp, GetTxResDTO tx) {
        // Save request size
        long processed = tx.getWif().size();
        // Iterate until data is exhausted given a previous request
        while (tx.getLinks().getNext() != null) {
            // Next request
            tx = processor.getTransactions(tx.getLinks().getNext());
            // Save request size
            processed += tx.getWif().size();
            // Save transaction
            tx.getWif().forEach(wif -> transaction.saveEventFhir(wif, timestamp));
        }
        return processed;
    }

    private long onDelete(OffsetDateTime timestamp) {
        return processor.deleteTransactions(timestamp).getDeletedTransactions();
    }
}
