 it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

public final class ClientRoutes {

    private ClientRoutes() {}

    @NoArgsConstructor(access = PRIVATE)
    public static final class Processor {
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

    @NoArgsConstructor(access = PRIVATE)
    public static final class Config {
        // COMMON
        public static final String IDENTIFIER_MS = "cfg";
        public static final String IDENTIFIER = "[CFG]";
        // ENDPOINT
        public static final String API_VERSION = "v1";
        public static final String API_CONFIG_ITEMS = "config-items";
        public static final String API_PROPS = "props";
        public static final String API_STATUS = "status";
        // QP
        public static final String QP_TYPE = "type";
        public static final String QP_PROPS = "props";
        // VALUES
        public static final String PROPS_NAME_EXP_DAYS = "expiring-date-day";
        public static final String PROPS_NAME_ISSUER_CF = "issuer-cf-cleaning";
        public static final String PROPS_NAME_SUBJECT = "subject-cleaning";
    }
}
