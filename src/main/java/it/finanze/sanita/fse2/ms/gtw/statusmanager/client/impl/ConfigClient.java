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

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.ConfigClientRoutes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes.Config.*;

@Slf4j
@Component
public class ConfigClient implements IConfigClient {

    @Autowired
    private RestTemplate client;

    @Autowired
    private ConfigClientRoutes routes;

    @Override
    public Integer getExpirationDate() {
    	int output = 5;

        String endpoint = routes.getStatusManagerConfig(PROPS_NAME_EXP_DAYS);
        log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

    	if(isReachable()) {
    		ResponseEntity<String> response = client.getForEntity(endpoint, String.class);
    		if(response.getBody() != null) output = Integer.parseInt(response.getBody());
    	}

        return output;
    }

    @Override
    public Boolean isSubjectPersistenceEnabled() {
        boolean output = false;
        String endpoint = routes.getStatusManagerConfig(PROPS_NAME_SUBJECT);
        log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

        if (isReachable()){
            ResponseEntity<String> response = client.getForEntity(endpoint, String.class);
            if (response.getBody() != null) output = Boolean.parseBoolean(response.getBody());
        }

        return output;
    }

    @Override
    public Boolean isCfOnIssuerAllowed() {
        boolean output = false;
        String endpoint = routes.getStatusManagerConfig(PROPS_NAME_ISSUER_CF);
        log.debug("{} - Executing request: {}", routes.identifier(), endpoint);

        if(isReachable()) {
            ResponseEntity<String> response = client.getForEntity(endpoint, String.class);
            if(response.getBody() != null) output = Boolean.parseBoolean(response.getBody());
        }

        return output;
    }

    private boolean isReachable() {
        boolean out;
        try {
            client.getForEntity(routes.status(), String.class);
            out = true;
        } catch (ResourceAccessException clientException) {
            out = false;
        }
        return out;
    }
}
