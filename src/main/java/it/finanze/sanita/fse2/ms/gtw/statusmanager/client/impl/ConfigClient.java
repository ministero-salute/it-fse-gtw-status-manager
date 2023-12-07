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
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ConfigClient implements IConfigClient {

    @Autowired
    private RestTemplate client;

    @Autowired
    private ConfigClientRoutes routes;

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

    @Override
    public ConfigItemDTO getConfigurationItems(ConfigItemTypeEnum type) {
        return client.getForObject(routes.getConfigItems(type), ConfigItemDTO.class);
    }

    @Override
    public String getProps(ConfigItemTypeEnum type, String props, String previous) {
        String out = previous;
        String endpoint = routes.getConfigItem(type, props);
        if (isReachable()) out = client.getForObject(endpoint, String.class);
        if(out == null || !out.equals(previous)) {
            log.info("[GTW-CFG] Property {} is set as {} (previously: {})", props, out, previous);
        }
        return out;
    }
}
