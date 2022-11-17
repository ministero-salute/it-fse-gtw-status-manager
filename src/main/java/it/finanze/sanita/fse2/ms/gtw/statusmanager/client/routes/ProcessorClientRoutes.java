package it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.MicroservicesURLCFG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes.Processor.*;


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

    public String getTransactions(int page, int limit, Date timestamp) {
        return base()
            .pathSegment(API_VERSION, TRANSACTIONS_PATH)
            .queryParam(TIMESTAMP_QP, timestamp)
            .queryParam(PAGE_QP, page)
            .queryParam(LIMIT_QP, limit)
            .build().toUriString();
    }

    public String deleteTransactions(Date timestamp) {
        return base()
            .pathSegment(API_VERSION, TRANSACTIONS_PATH)
            .queryParam(TIMESTAMP_QP, timestamp)
            .build().toUriString();
    }

}
