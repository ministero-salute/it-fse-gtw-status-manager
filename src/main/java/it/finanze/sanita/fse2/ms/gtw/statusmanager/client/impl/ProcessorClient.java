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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.client.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IProcessorClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.ProcessorClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
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

    public static final int CHUNK_LIMIT = 10000;

    @Autowired
    private RestTemplate client;

    @Autowired
    private ProcessorClientRoutes routes;

    @Override
    public GetTxResDTO getTransactions(OffsetDateTime timestamp, int page, int limit) {

        String endpoint = routes.getTransactions(page, limit, timestamp);
        log.debug(Constants.Logs.EXECUTE_REQUEST, routes.identifier(), endpoint);

        // Execute request
        ResponseEntity<GetTxResDTO> response = client.getForEntity(
            endpoint,
            GetTxResDTO.class
        );

        return response.getBody();
    }

    @Override
    public GetTxResDTO getTransactions(String url) {
        log.debug(Constants.Logs.EXECUTE_REQUEST, routes.identifier(), url);

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
        log.debug(Constants.Logs.EXECUTE_REQUEST, routes.identifier(), endpoint);

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
