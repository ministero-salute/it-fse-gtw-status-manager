package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes.Config.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl.ConfigSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.ProfileUtility;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(ConfigServiceIsolationTest.Context.class)
@TestPropertySource(properties = "ms.config.refresh-rate=900000")
class ConfigServiceIsolationTest {
    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    static class Context {
        @org.springframework.context.annotation.Bean
        ConfigSRV configService() { return new ConfigSRV(); }
        @org.springframework.context.annotation.Bean
        ProfileUtility profileUtility() {
            ProfileUtility profiles = mock(ProfileUtility.class);
            when(profiles.isTestProfile()).thenReturn(true);
            return profiles;
        }
    }

    @Autowired ConfigSRV service;
    @Autowired ProfileUtility profiles;
    @MockitoBean IConfigClient client;

    @Test
    void testProfileSkipsRemoteInitialization() {
        service.postConstruct();
        verifyNoInteractions(client);
        assertEquals(900000L, service.getRefreshRate());
    }

    @Test
    void loadsGenericThenSpecificOverridesAndCachesUntilRefresh() {
        try {
            initialize();
            assertEquals(7, service.getExpirationDate());
            assertTrue(service.isCfOnIssuerNotAllowed());
            assertFalse(service.isSubjectNotAllowed());
            verify(client, never()).getProps(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            org.springframework.test.util.ReflectionTestUtils.setField(service, "refreshRate", 0L);
            when(client.getProps(PROPS_NAME_EXP_DAYS, "7", ConfigItemTypeEnum.STATUS_MANAGER)).thenReturn("14");
            when(client.getProps(PROPS_NAME_SUBJECT, "false", ConfigItemTypeEnum.STATUS_MANAGER)).thenReturn("true");
            when(client.getProps(PROPS_NAME_ISSUER_CF, "true", ConfigItemTypeEnum.STATUS_MANAGER)).thenReturn("false");
            assertEquals(14, service.getExpirationDate());
            assertTrue(service.isSubjectNotAllowed());
            assertFalse(service.isCfOnIssuerNotAllowed());
        } finally {
            when(profiles.isTestProfile()).thenReturn(true);
            org.springframework.test.util.ReflectionTestUtils.setField(service, "refreshRate", 900000L);
            ((Map<?, ?>) org.springframework.test.util.ReflectionTestUtils.getField(service, "props")).clear();
        }
    }

    @Test
    void missingRequiredConfigurationFailsInitialization() {
        try {
            when(profiles.isTestProfile()).thenReturn(false);
            var empty = items(Map.of()); empty.setConfigurationItems(List.of());
            when(client.getConfigurationItems(org.mockito.ArgumentMatchers.any())).thenReturn(empty);
            assertThrows(IllegalStateException.class, service::postConstruct);
        } finally { when(profiles.isTestProfile()).thenReturn(true); }
    }

    private void initialize() {
        when(profiles.isTestProfile()).thenReturn(false);
        when(client.getConfigurationItems(ConfigItemTypeEnum.GENERIC)).thenReturn(items(Map.of(
            PROPS_NAME_EXP_DAYS, "3", PROPS_NAME_SUBJECT, "false", PROPS_NAME_ISSUER_CF, "true")));
        when(client.getConfigurationItems(ConfigItemTypeEnum.STATUS_MANAGER)).thenReturn(items(Map.of(PROPS_NAME_EXP_DAYS, "7")));
        service.postConstruct();
    }

    private ConfigItemDTO items(Map<String, String> values) {
        var item = new ConfigItemDTO.ConfigDataItemDTO(); item.setItems(values);
        var response = new ConfigItemDTO(); response.setConfigurationItems(List.of(item)); return response;
    }
}
