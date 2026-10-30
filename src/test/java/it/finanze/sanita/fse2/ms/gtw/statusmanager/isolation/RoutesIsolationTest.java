package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.util.UriComponentsBuilder;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.*;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({ConfigClientRoutes.class, ProcessorClientRoutes.class, EdsClientRoutes.class, MicroservicesURLCFG.class})
@TestPropertySource(properties = {"ms.url.gtw-config=https://config.example/base", "ms.url.eds-broker=https://processor.example",
    "ms.url.eds-client=https://eds.example"})
class RoutesIsolationTest {
    @Autowired ConfigClientRoutes config;
    @Autowired ProcessorClientRoutes processor;
    @Autowired EdsClientRoutes eds;

    @Test
    void configRoutesKeepBasePathAndSelectTypeAndProperty() {
        assertEquals("https://config.example/base/status", config.status());
        assertEquals("https://config.example/base/v1/config-items?type=GENERIC", config.getConfigItems(ConfigItemTypeEnum.GENERIC));
        assertEquals("https://config.example/base/v1/config-items/props?type=STATUS_MANAGER&props=expiry",
            config.getConfigItem(ConfigItemTypeEnum.STATUS_MANAGER, "expiry"));
        assertEquals("[CFG]", config.identifier()); assertEquals("cfg", config.microservice());
    }

    @Test
    void processorRoutesPreserveTimestampPaginationAndRegionalScope() {
        var timestamp = OffsetDateTime.parse("2024-01-01T01:00:00+01:00");
        var route = UriComponentsBuilder.fromUriString(processor.getTransactions(2, 50, timestamp, "010")).build();
        assertEquals("/v1/transactions/rda/010", route.getPath());
        assertEquals("2", route.getQueryParams().getFirst("page"));
        assertEquals("50", route.getQueryParams().getFirst("limit"));
        assertEquals("2024-01-01T01:00:00+01:00", route.getQueryParams().getFirst("timestamp"));
        var deletion = UriComponentsBuilder.fromUriString(processor.deleteTransactions(timestamp, "010")).build();
        assertEquals(route.getPath(), deletion.getPath());
        assertFalse(deletion.getQueryParams().containsKey("page"));
        assertEquals("[PRC]", processor.identifier()); assertEquals("processor", processor.microservice());
    }

    @Test
    void edsUsesVersionedStatusAndWorkflowPath() {
        assertEquals("https://eds.example/v1/status/workflow", eds.getIngestionStatus("workflow"));
        assertEquals("[EDS]", eds.identifier()); assertEquals("eds-client", eds.microservice());
    }
}
