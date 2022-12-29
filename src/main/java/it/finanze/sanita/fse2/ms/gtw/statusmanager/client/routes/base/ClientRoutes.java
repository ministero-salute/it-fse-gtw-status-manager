/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
