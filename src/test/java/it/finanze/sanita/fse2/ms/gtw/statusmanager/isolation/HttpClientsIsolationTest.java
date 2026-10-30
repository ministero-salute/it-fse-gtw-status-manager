package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.impl.*;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.*;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.eds.GetIngestionStatusResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.*;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.*;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({ConfigClient.class, ProcessorClient.class, EdsClient.class})
class HttpClientsIsolationTest {
    @Autowired ConfigClient config;
    @Autowired ProcessorClient processor;
    @Autowired EdsClient eds;
    @MockitoBean RestTemplate http;
    @MockitoBean ConfigClientRoutes configRoutes;
    @MockitoBean ProcessorClientRoutes processorRoutes;
    @MockitoBean EdsClientRoutes edsRoutes;

    @Test
    void configPropertyUsesSpecificThenGenericAndKeepsPreviousWhenOffline() {
        when(configRoutes.status()).thenReturn("/status");
        when(configRoutes.getConfigItem(ConfigItemTypeEnum.STATUS_MANAGER, "property")).thenReturn("/specific");
        when(configRoutes.getConfigItem(ConfigItemTypeEnum.GENERIC, "property")).thenReturn("/generic");
        when(http.getForObject("/specific", String.class)).thenReturn("specific");
        assertEquals("specific", config.getProps("property", "previous", ConfigItemTypeEnum.STATUS_MANAGER));
        verify(http, never()).getForObject("/generic", String.class);
        when(http.getForObject("/specific", String.class)).thenReturn(null);
        when(http.getForObject("/generic", String.class)).thenReturn("generic");
        assertEquals("generic", config.getProps("property", "previous", ConfigItemTypeEnum.STATUS_MANAGER));
        when(http.getForObject("/generic", String.class)).thenReturn(null);
        assertNull(config.getProps("property", "previous", ConfigItemTypeEnum.STATUS_MANAGER));
        when(http.getForEntity("/status", String.class)).thenThrow(new ResourceAccessException("offline"));
        assertEquals("previous", config.getProps("property", "previous", ConfigItemTypeEnum.STATUS_MANAGER));
    }

    @Test
    void configItemsReturnsRemoteBody() {
        when(configRoutes.getConfigItems(ConfigItemTypeEnum.GENERIC)).thenReturn("/items");
        var items = new ConfigItemDTO();
        when(http.getForObject("/items", ConfigItemDTO.class)).thenReturn(items);
        assertSame(items, config.getConfigurationItems(ConfigItemTypeEnum.GENERIC));
    }

    @Test
    void edsReturnsResponseAndDistinguishesNetworkFromOtherHttpFailures() {
        when(edsRoutes.getIngestionStatus("workflow")).thenReturn("/eds/workflow");
        var status = GetIngestionStatusResDTO.builder().status("SUCCESS").build();
        when(http.getForEntity("/eds/workflow", GetIngestionStatusResDTO.class)).thenReturn(ResponseEntity.ok(status));
        assertSame(status, eds.getIngestionStatus("workflow"));
        var network = new ResourceAccessException("offline");
        when(http.getForEntity("/eds/workflow", GetIngestionStatusResDTO.class)).thenThrow(network);
        assertSame(network, assertThrows(RemoteServiceNotAvailableException.class, () -> eds.getIngestionStatus("workflow")).getCause());
        var failure = new RestClientException("bad response");
        doThrow(failure).when(http).getForEntity("/eds/workflow", GetIngestionStatusResDTO.class);
        assertSame(failure, assertThrows(BusinessException.class, () -> eds.getIngestionStatus("workflow")).getCause());
    }

    @Test
    void processorUsesPagedAndContinuationRoutesAndDeleteMethod() {
        var timestamp = OffsetDateTime.parse("2024-01-01T00:00:00Z");
        when(processorRoutes.getTransactions(2, 100, timestamp, "010")).thenReturn("/transactions");
        when(processorRoutes.deleteTransactions(timestamp, "010")).thenReturn("/delete");
        var page = new GetTxResDTO(); var deleted = new DeleteTxResDTO();
        when(http.getForEntity("/transactions", GetTxResDTO.class)).thenReturn(ResponseEntity.ok(page));
        when(http.getForEntity("/next", GetTxResDTO.class)).thenReturn(ResponseEntity.ok(page));
        when(http.exchange("/delete", HttpMethod.DELETE, null, DeleteTxResDTO.class)).thenReturn(ResponseEntity.ok(deleted));
        assertSame(page, processor.getTransactions(timestamp, 2, 100, "010"));
        assertSame(page, processor.getTransactions("/next", "010"));
        assertSame(deleted, processor.deleteTransactions(timestamp, "010"));
        verify(http).exchange("/delete", HttpMethod.DELETE, null, DeleteTxResDTO.class);
    }
}
