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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.MicroservicesURLCFG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes.Processor.*;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;


@Component
public final class ProcessorClientRoutes {

    @Autowired
    private MicroservicesURLCFG microservices;

    public UriComponentsBuilder base() {
        return UriComponentsBuilder.fromHttpUrl(microservices.getProcessorHost());
    }

    public String identifier() {
        return IDENTIFIER;
    }

    public String microservice() {
        return IDENTIFIER_MS;
    }

    public String getTransactions(int page, int limit, OffsetDateTime timestamp) {
        return base()
            .pathSegment(API_VERSION, TRANSACTIONS_PATH)
            .queryParam(TIMESTAMP_QP, ISO_DATE_TIME.format(timestamp))
            .queryParam(PAGE_QP, page)
            .queryParam(LIMIT_QP, limit)
            .build().toUriString();
    }

    public String deleteTransactions(OffsetDateTime timestamp) {
        return base()
            .pathSegment(API_VERSION, TRANSACTIONS_PATH)
            .queryParam(TIMESTAMP_QP, ISO_DATE_TIME.format(timestamp))
            .build().toUriString();
    }

}
