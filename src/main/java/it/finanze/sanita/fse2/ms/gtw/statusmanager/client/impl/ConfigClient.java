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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.MicroservicesURLCFG;

@Component
public class ConfigClient implements IConfigClient {

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private MicroservicesURLCFG msUrlCFG;

    @Override
    public Integer getExpirationDate() {
    	Integer output = 5;
    	
    	if(isReachable()) {
    		String endpoint = msUrlCFG.getConfigHost() + "/v1/config-items/props?type=STATUS_MANAGER&props=expiring_date_day";
    		ResponseEntity<String> response = restTemplate.getForEntity(endpoint,String.class);
    		if(response.getBody() != null) {
    			output = Integer.parseInt(response.getBody());
    		}
    	}
        return output;
    }
 
    private boolean isReachable() {
        try {
            final String endpoint = msUrlCFG.getConfigHost() + ClientRoutes.Config.STATUS_PATH;
            restTemplate.getForEntity(endpoint, String.class);
            return true;
        } catch (ResourceAccessException clientException) {
            return false;
        }
    }
}
