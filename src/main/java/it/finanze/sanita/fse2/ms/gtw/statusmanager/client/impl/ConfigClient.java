/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.MicroservicesURLCFG;

@Component
public class ConfigClient implements IConfigClient {

    @Autowired
    private RestTemplate client;
    
    @Autowired
    private MicroservicesURLCFG msUrlCFG;

    @Override
    public Integer getExpirationDate() {
    	Integer output = 0;
        String endpoint = msUrlCFG.getConfigHost() + "/v1/config-items/props?type=STATUS_MANAGER&props=expiring_date_day";
        ResponseEntity<String> response = client.getForEntity(endpoint,String.class);
        if(response.getBody()!=null) {
        	output = Integer.parseInt(response.getBody());
        }
        return output;
    }
 
}
