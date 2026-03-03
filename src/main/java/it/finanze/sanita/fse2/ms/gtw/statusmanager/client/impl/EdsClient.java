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

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.EdsClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.eds.GetIngestionStatusResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.RemoteServiceNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class EdsClient implements IEdsClient {

    @Autowired
    private RestTemplate client;

    @Autowired
    private EdsClientRoutes routes;

    @Override
    public GetIngestionStatusResDTO getIngestionStatus(String workflowInstanceId) {
        String endpoint = routes.getIngestionStatus(workflowInstanceId);
        log.debug(Constants.Logs.EXECUTE_REQUEST, routes.identifier(), endpoint);

        try {
            // Execute request
            ResponseEntity<GetIngestionStatusResDTO> response = client.getForEntity(
                    endpoint,
                    GetIngestionStatusResDTO.class);

            return response.getBody();
        } catch (ResourceAccessException ex) {
            // Network/connection issues - wrap in RemoteServiceNotAvailableException for retry logic
            log.error("Remote service not available for workflowInstanceId: {}", workflowInstanceId, ex);
            throw new RemoteServiceNotAvailableException("EDS service not available", ex);
        } catch (RestClientException ex) {
            // Other REST client errors - wrap in BusinessException
            log.error("Error calling EDS service for workflowInstanceId: {}", workflowInstanceId, ex);
            throw new BusinessException("Error retrieving ingestion status from EDS", ex);
        }
    }
}
