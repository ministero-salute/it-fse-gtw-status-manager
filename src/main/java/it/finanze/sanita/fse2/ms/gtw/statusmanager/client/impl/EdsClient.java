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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

        // Execute request
        ResponseEntity<GetIngestionStatusResDTO> response = client.getForEntity(
                endpoint,
                GetIngestionStatusResDTO.class);

        return response.getBody();
    }
}
