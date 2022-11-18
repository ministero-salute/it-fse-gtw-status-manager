/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.impl.TxExecutor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes.EMPTY;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.impl.TxExecutor.TITLE;


@Slf4j
@Component
public class TxScheduler {

    @Autowired
    private TxExecutor tx;

    @Scheduled(cron = "${scheduler.tx-scheduler}")
    @SchedulerLock(name = "invokeTxScheduler", lockAtMostFor = "60m")
    public void action() {
        log.info("[{}] Starting updating process", TITLE);
        if (tx.execute() == EMPTY) {
            log.info("[{}] - No transactions to process", TITLE);
        }
        log.debug("[{}] Ending updating process", TITLE);
    }

}
