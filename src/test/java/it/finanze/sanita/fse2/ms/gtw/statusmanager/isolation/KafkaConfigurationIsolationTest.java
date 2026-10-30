package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka.*;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka.oauth2.CustomAuthenticateCallbackHandler;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(KafkaConfigurationIsolationTest.Context.class)
class KafkaConfigurationIsolationTest {
    // Plain configuration instances allow each scenario to build its own factories.
    // No @EnableKafka and no producer/consumer is ever created or started.
    @Configuration(proxyBeanMethods = false)
    static class Context {
        @Bean KafkaProducerCFG producerConfiguration() { return new KafkaProducerCFG(); }
        @Bean KafkaConsumerCFG consumerConfiguration() { return new KafkaConsumerCFG(); }
    }

    @Autowired KafkaProducerCFG producer;
    @Autowired KafkaConsumerCFG consumer;
    @MockitoBean KafkaPropertiesCFG common;
    @MockitoBean KafkaProducerPropertiesCFG producerProperties;
    @MockitoBean KafkaConsumerPropertiesCFG consumerProperties;
    @MockitoBean KafkaTopicCFG topics;

    @BeforeEach
    void properties() {
        when(common.getBootstrapServers()).thenReturn("unused.invalid:9092");
        when(producerProperties.getClientId()).thenReturn("status-manager");
        when(producerProperties.getProducerRetries()).thenReturn("3");
        when(producerProperties.getProducerKeySerializer()).thenReturn("org.apache.kafka.common.serialization.StringSerializer");
        when(producerProperties.getProducerValueSerializer()).thenReturn("org.apache.kafka.common.serialization.StringSerializer");
        when(consumerProperties.getClientId()).thenReturn("status-manager-consumer");
        when(consumerProperties.getConsumerGroupId()).thenReturn("status-group");
        when(consumerProperties.getConsumerKeyDeserializer()).thenReturn("org.apache.kafka.common.serialization.StringDeserializer");
        when(consumerProperties.getConsumerValueDeserializer()).thenReturn("org.apache.kafka.common.serialization.StringDeserializer");
        when(consumerProperties.getIsolationLevel()).thenReturn("read_committed");
        when(consumerProperties.getAutoCommit()).thenReturn("false");
        when(consumerProperties.getAutoOffsetReset()).thenReturn("earliest");
        when(consumerProperties.getRetryIntervalMs()).thenReturn(0);
        when(consumerProperties.getNRetry()).thenReturn(2);
        when(consumerProperties.getDeadLetterExceptions()).thenReturn(List.of(IllegalStateException.class.getName()));
        when(topics.getStatusManagerTopicDlt()).thenReturn("status-events-dlt");
    }

    @Test
    void unsecuredFactoriesKeepDeliveryAndConsumerOffsetSettingsWithoutSecurityKeys() {
        Map<String, Object> sending = producer.producerWithoutTransactionConfigs();
        assertEquals("status-manager-noTx", sending.get(ProducerConfig.CLIENT_ID_CONFIG));
        assertEquals("3", sending.get(ProducerConfig.RETRIES_CONFIG));
        assertEquals("unused.invalid:9092", sending.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("org.apache.kafka.common.serialization.StringSerializer", sending.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
        Map<String, Object> receiving = consumer.consumerFactory().getConfigurationProperties();
        assertEquals("status-manager-consumer", receiving.get(ConsumerConfig.CLIENT_ID_CONFIG));
        assertEquals("status-group", receiving.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("read_committed", receiving.get(ConsumerConfig.ISOLATION_LEVEL_CONFIG));
        assertEquals("false", receiving.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));
        assertEquals("earliest", receiving.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertEquals("org.apache.kafka.common.serialization.StringDeserializer", receiving.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
        assertTrue(receiving.get(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG).toString().contains("TracingConsumerInterceptor"));
        for (Map<String, Object> properties : List.of(sending, receiving)) {
            assertFalse(properties.containsKey("security.protocol"));
            assertFalse(properties.containsKey("sasl.mechanism"));
            assertFalse(properties.containsKey("sasl.jaas.config"));
            assertFalse(properties.containsKey("ssl.truststore.location"));
            assertFalse(properties.containsKey("ssl.truststore.password"));
            assertFalse(properties.containsKey("sasl.login.callback.handler.class"));
        }
        assertEquals(sending, producer.producerDeadFactory().getConfigurationProperties());
        assertEquals(sending, producer.producerFactoryWithoutTransaction().getConfigurationProperties());
        assertEquals(sending, producer.noTxKafkaDeadTemplate().getProducerFactory().getConfigurationProperties());
        assertEquals(sending, producer.notxKafkaTemplate().getProducerFactory().getConfigurationProperties());
        var container = (AbstractMessageListenerContainer<?, ?>) consumer.kafkaListenerContainerFactory(mock(KafkaTemplate.class)).createContainer("events");
        assertFalse(container.isRunning());
        assertNull(container.getCommonErrorHandler());
    }

    @Test
    void oauthSecurityIsPassedToBothFactoriesIncludingCredentialsAndCallbacks() {
        when(common.getProtocol()).thenReturn("SASL_SSL");
        when(consumerProperties.getProtocol()).thenReturn("SASL_SSL");
        when(common.getMechanism()).thenReturn("OAUTHBEARER");
        when(common.getConfigJaas()).thenReturn("oauth-login required;");
        when(common.getTrustoreLocation()).thenReturn("/not-opened/truststore.p12");
        when(common.getTrustorePassword()).thenReturn("secret".toCharArray());
        when(common.getTenantId()).thenReturn("tenant");
        when(common.getAppId()).thenReturn("application");
        when(common.getPfxPathName()).thenReturn("/not-opened/client.pfx");
        when(common.getPwd()).thenReturn("certificate-password");
        when(common.getCallbackHandlerClass()).thenReturn("example.Callback");
        for (Map<String, Object> properties : List.of(producer.producerWithoutTransactionConfigs(), consumer.consumerFactory().getConfigurationProperties())) {
            assertEquals("SASL_SSL", properties.get("security.protocol"));
            assertEquals("OAUTHBEARER", properties.get("sasl.mechanism"));
            assertEquals("oauth-login required;", properties.get("sasl.jaas.config"));
            assertEquals("/not-opened/truststore.p12", properties.get("ssl.truststore.location"));
            assertEquals("secret", properties.get("ssl.truststore.password"));
            assertEquals(CustomAuthenticateCallbackHandler.class, properties.get("sasl.login.callback.handler.class"));
            assertEquals("tenant", properties.get("kafka.oauth.tenantId"));
            assertEquals("application", properties.get("kafka.oauth.appId"));
            assertEquals("/not-opened/client.pfx", properties.get("kafka.oauth.pfxPathName"));
            assertEquals("certificate-password", properties.get("kafka.oauth.pwd"));
            assertEquals("example.Callback", properties.get("sasl.client.callback.handler.class"));
        }
    }

    @Test
    void emptySecurityOptionsAreOmittedAndPlainSaslDoesNotConfigureOauth() {
        when(common.getProtocol()).thenReturn("");
        when(consumerProperties.getProtocol()).thenReturn("");
        when(common.getConfigJaas()).thenReturn("");
        when(common.getTrustoreLocation()).thenReturn("");
        when(common.getTrustorePassword()).thenReturn(new char[0]);
        when(common.getCallbackHandlerClass()).thenReturn("");
        when(common.getMechanism()).thenReturn("PLAIN");
        for (Map<String, Object> properties : List.of(producer.producerWithoutTransactionConfigs(), consumer.consumerFactory().getConfigurationProperties())) {
            assertEquals("PLAIN", properties.get("sasl.mechanism"));
            assertFalse(properties.containsKey("security.protocol"));
            assertFalse(properties.containsKey("ssl.truststore.password"));
            assertFalse(properties.containsKey("sasl.login.callback.handler.class"));
            assertFalse(properties.containsKey("sasl.client.callback.handler.class"));
        }
    }

    @Test
    void configuredNonRetryableFailurePublishesOriginalRecordToDeadLetterImmediately() {
        KafkaTemplate<Object, Object> kafka = mock(KafkaTemplate.class);
        when(kafka.send(org.mockito.ArgumentMatchers.any(ProducerRecord.class)))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        var container = consumer.kafkaListenerDeadLetterContainerFactory(kafka).createContainer("events");
        DefaultErrorHandler handler = assertInstanceOf(DefaultErrorHandler.class, container.getCommonErrorHandler());
        Consumer<Object, Object> kafkaConsumer = mock(Consumer.class);
        assertTrue(handler.handleOne(new IllegalStateException("invalid event"),
            new ConsumerRecord<>("events", 0, 42, "workflow", "payload"), kafkaConsumer, container));
        var sent = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafka).send(sent.capture());
        assertEquals("status-events-dlt", sent.getValue().topic());
        assertNull(sent.getValue().partition()); // -1 resolver lets Kafka select the DLT partition.
        assertEquals("workflow", sent.getValue().key());
        assertEquals("payload", sent.getValue().value());
        verifyNoInteractions(kafkaConsumer);
        assertFalse(container.isRunning());
    }

    @Test
    void invalidExceptionClassDoesNotPreventBuildingErrorHandler() {
        when(consumerProperties.getDeadLetterExceptions()).thenReturn(List.of("missing.Exception"));
        var container = (AbstractMessageListenerContainer<?, ?>) consumer.kafkaListenerDeadLetterContainerFactory(mock(KafkaTemplate.class)).createContainer("events");
        assertNotNull(container.getCommonErrorHandler());
        assertFalse(container.isRunning());
    }
}
