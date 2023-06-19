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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base;

public final class ClientRoutes {

    private ClientRoutes() {}

    public static final class Processor {

        private Processor() {}
        // COMMON
        public static final String IDENTIFIER = "[PRC]";
        public static final String IDENTIFIER_MS = "processor";
        // PATH PARAMS
        public static final String TIMESTAMP_QP = "timestamp";
        public static final String PAGE_QP = "page";
        public static final String LIMIT_QP = "limit";
        // ENDPOINT
        public static final String API_VERSION = "v1";
        public static final String TRANSACTIONS_PATH = "transactions";
        
    }
    
    public static final class Config {

        private Config() {}
        public static final String STATUS_PATH = "/status";
    }

}
