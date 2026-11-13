package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.*;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.MicroservicesURLCFG;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({ConfigClientRoutes.class, EdsClientRoutes.class, MicroservicesURLCFG.class})
@TestPropertySource(properties = {"ms.url.gtw-config=https://config.example/base", "ms.url.eds-client=https://eds.example"})
class RoutesIsolationTest {
    @Autowired ConfigClientRoutes config;
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
    void edsUsesVersionedStatusAndWorkflowPath() {
        assertEquals("https://eds.example/v1/status/workflow", eds.getIngestionStatus("workflow"));
        assertEquals("[EDS]", eds.identifier()); assertEquals("eds-client", eds.microservice());
    }
}
