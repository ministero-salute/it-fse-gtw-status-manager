/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IProcessorClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.DeleteTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.SubjectOrganizationEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.OperationException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.DateUtility;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.impl.ProcessorClient.CHUNK_LIMIT;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.DateUtility.getCurrentTime;

@Slf4j
@Component
public class TxExecutor {

    private static final List<SubjectOrganizationEnum> REGIONS = List.of(
            SubjectOrganizationEnum.REGIONE_PIEMONTE,
            SubjectOrganizationEnum.REGIONE_VALLE_AOSTA,
            SubjectOrganizationEnum.REGIONE_LOMBARDIA,
            SubjectOrganizationEnum.REGIONE_BOLZANO,
            SubjectOrganizationEnum.REGIONE_TRENTO,
            SubjectOrganizationEnum.REGIONE_VENETO,
            SubjectOrganizationEnum.REGIONE_FRIULI_VENEZIA_GIULIA,
            SubjectOrganizationEnum.REGIONE_LIGURIA,
            SubjectOrganizationEnum.REGIONE_EMILIA_ROMAGNA,
            SubjectOrganizationEnum.REGIONE_TOSCANA,
            SubjectOrganizationEnum.REGIONE_UMBRIA,
            SubjectOrganizationEnum.REGIONE_MARCHE,
            SubjectOrganizationEnum.REGIONE_LAZIO,
            SubjectOrganizationEnum.REGIONE_ABRUZZO,
            SubjectOrganizationEnum.REGIONE_MOLISE,
            SubjectOrganizationEnum.REGIONE_CAMPANIA,
            SubjectOrganizationEnum.REGIONE_PUGLIA,
            SubjectOrganizationEnum.REGIONE_BASILICATA,
            SubjectOrganizationEnum.REGIONE_CALABRIA,
            SubjectOrganizationEnum.REGIONE_SICILIA,
            SubjectOrganizationEnum.REGIONE_SARDEGNA);

    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 2_000;

    private final IProcessorClient processorClient;
    private final ITransactionEventsRepo transactionRepo;
    private final IConfigSRV configSRV;

    public TxExecutor(IProcessorClient processor,
            ITransactionEventsRepo transactionRepo,
            IConfigSRV configSRV) {
        this.processorClient = processor;
        this.transactionRepo = transactionRepo;
        this.configSRV = configSRV;
    }

    @Scheduled(cron = "${scheduler.tx-scheduler}")
    @SchedulerLock(name = "txScheduler", lockAtMostFor = "15m", lockAtLeastFor = "1m")
    public void run() {
        log.info("[TX] Start sequential run for {} region(s)", REGIONS.size());

        List<RegionResult> results = new ArrayList<>(REGIONS.size());

        for (var region : REGIONS) {
            final OffsetDateTime ts = getCurrentTime();
            log.info("[TX] Processing region {} with timestamp={}", region, ts);
            RegionResult result = processRegion(region, ts);
            results.add(result);
        }

        long ok = results.stream().filter(r -> r.error == null).count();
        long ko = results.size() - ok;

        log.info("[TX] Sequential run completed: success={}, failed={}", ok, ko);
        results.stream()
                .filter(r -> r.error != null)
                .forEach(r -> log.error("[TX] Region {} ({}): FAILED — {}", r.name(), r.code(), r.error.toString(),
                        r.error));
    }

    private RegionResult processRegion(SubjectOrganizationEnum region, OffsetDateTime ts) {
        final String code = region.getCode();
        final String name = region.getDisplay();

        try {
            log.info("[TX] ==> Begin region {} ({})", name, code);

            long processed = fetchAndProcessAll(ts, code);
            log.debug("[TX] [{}:{}] Processed transactions: {}", name, code, processed);

            DeleteTxResDTO del = retry("DELETE-" + code, () -> processorClient.deleteTransactions(ts, code));
            long deleted = (del != null) ? del.getDeletedTransactions() : 0L;
            log.debug("[TX] [{}:{}] Deleted transactions: {}", name, code, deleted);

            log.info("[TX] <== End   region {} ({})", name, code);
            return new RegionResult(region, processed, deleted, null);
        } catch (Exception ex) {
            log.error("[TX] Error on region {} ({})", name, code, ex);
            return new RegionResult(region, 0L, 0L, ex);
        }
    }

    /**
     * Fetch and process all pages for one region (retries around network calls).
     * 
     * @throws OperationException
     */
    private long fetchAndProcessAll(OffsetDateTime ts, String regionCode) throws OperationException {
        // first page
        GetTxResDTO page = retry("INIT-" + regionCode,
                () -> processorClient.getTransactions(ts, 0, CHUNK_LIMIT, regionCode));
        if (page == null || page.getWif() == null || page.getWif().isEmpty()) {
            log.info("[TX] [{}] No transactions to process", regionCode);
            return 0L;
        }
        long count = 0L;
        Date tsDate = Date.from(ts.toInstant());
        Date expDate = DateUtility.addDay(new Date(), configSRV.getExpirationDate());

        // first page
        count += transactionRepo.saveEventsFhir(page.getWif(), tsDate, expDate);

        // next pages
        while (page.getLinks() != null && page.getLinks().getNext() != null) {
            final String nextUrl = page.getLinks().getNext();
            page = retry("PAGE-" + regionCode, () -> processorClient.getTransactions(nextUrl, regionCode));
            if (page != null && page.getWif() != null && !page.getWif().isEmpty()) {
                count += transactionRepo.saveEventsFhir(page.getWif(), tsDate, expDate);
            }
        }
        return count;
    }

    // minimal retry helper
    private <T> T retry(String label, Supplier<T> op) {
        Exception lastEx = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                T res = op.get();
                if (res != null)
                    return res;
            } catch (Exception ex) {
                lastEx = ex;
            }
            backoff(label, attempt);
        }
        if (lastEx != null)
            // manage errors here,
            // consider putting unresolved operations into a staging area
            throw new RuntimeException(lastEx);
        return null;
    }

    private void backoff(String label, int attempt) {
        long sleep = BASE_BACKOFF_MS << (attempt - 1);
        log.warn("[TX] {} attempt {} failed, retrying in {} ms", label, attempt, sleep);
        try {
            TimeUnit.MILLISECONDS.sleep(sleep);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /** Small result holder for per-region reporting. */
    public static final class RegionResult {
        private final SubjectOrganizationEnum region;
        private final long processed;
        private final long deleted;
        private final Throwable error;

        RegionResult(SubjectOrganizationEnum region, long processed, long deleted, Throwable error) {
            this.region = region;
            this.processed = processed;
            this.deleted = deleted;
            this.error = error;
        }

        public SubjectOrganizationEnum region() {
            return region;
        }

        public long processed() {
            return processed;
        }

        public long deleted() {
            return deleted;
        }

        public Throwable error() {
            return error;
        }

        public String code() {
            return region.getCode();
        }

        public String name() {
            return region.getDisplay();
        }
    }

}
