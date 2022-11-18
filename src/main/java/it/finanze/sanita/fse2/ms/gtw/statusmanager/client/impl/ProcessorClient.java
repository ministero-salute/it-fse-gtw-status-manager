/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.client.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IProcessorClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.ProcessorClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.DeleteTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;

import static org.springframework.http.HttpMethod.DELETE;

@Slf4j
@Component
public class ProcessorClient implements IProcessorClient {

    public static final int CHUNK_LIMIT = 5;

    @Autowired
    private RestTemplate client;

    @Autowired
    private ProcessorClientRoutes routes;

    @Override
    public GetTxResDTO getTransactions(OffsetDateTime timestamp, int page, int limit) {

        String endpoint = routes.getTransactions(page, limit, timestamp);
        log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

        // Execute request
        ResponseEntity<GetTxResDTO> response = client.getForEntity(
            endpoint,
            GetTxResDTO.class
        );

        return response.getBody();
    }

    @Override
    public GetTxResDTO getTransactions(String url) {
        log.debug("{} - Executing request: {}", routes.identifier(), url);

        // Execute request
        ResponseEntity<GetTxResDTO> response = client.getForEntity(
            url,
            GetTxResDTO.class
        );

        return response.getBody();
    }

    @Override
    public DeleteTxResDTO deleteTransactions(OffsetDateTime timestamp) {

        String endpoint = routes.deleteTransactions(timestamp);
        log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

        // Execute request
        ResponseEntity<DeleteTxResDTO> response = client.exchange(
            endpoint,
            DELETE,
            null,
            DeleteTxResDTO.class
        );

        return response.getBody();
    }
}
