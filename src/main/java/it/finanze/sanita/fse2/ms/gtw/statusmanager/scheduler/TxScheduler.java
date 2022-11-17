package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TxScheduler {

    @Scheduled(cron = "${scheduler.tx-scheduler}")
    @SchedulerLock(name = "invokeTxScheduler" , lockAtMostFor = "60m")
    public void action() {
        System.out.println("TxScheduler");
    }
}
