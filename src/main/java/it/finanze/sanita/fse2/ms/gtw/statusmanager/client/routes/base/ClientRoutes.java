package it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base;

public final class ClientRoutes {

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

}
