package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IProcessorClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.DeleteTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.DateUtility;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.impl.ProcessorClient.CHUNK_LIMIT;

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
        // Working var
        OffsetDateTime timestamp = DateUtility.getCurrentTime();
        long processed = 0;
        log.info("invokeTxScheduler - Start / {}", timestamp);
        // Get chunk
        GetTxResDTO tx = processor.getTransactions(timestamp, 0, CHUNK_LIMIT);
        // Check if skipping
        if(!tx.getWif().isEmpty()) {
            // Save request size
            processed += tx.getWif().size();
            // Iterate until exhausting data
            while (tx.getLinks().getNext() != null) {
                // Next request
                tx = processor.getTransactions(tx.getLinks().getNext());
                // Save request size
                processed += tx.getWif().size();
                // Save transaction
                tx.getWif().forEach(wif -> transaction.saveEvent(wif, "FHIR_SUCCESS", "SUCCESS", timestamp));
            }
            log.info("invokeTxScheduler - Updated transactions: {}", processed);
            // Call delete
            DeleteTxResDTO out = processor.deleteTransactions(timestamp);
            log.info("invokeTxScheduler - Deleted transactions: {}", out.getDeletedTransactions());
        }else {
            log.info("invokeTxScheduler - No transactions to process");
        }
        log.info("invokeTxScheduler - Finish");
    }
}
