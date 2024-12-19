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
package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl.ConfigSRV;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Description;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IKafkaReceiverSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
@EmbeddedKafka
class KafkaReceiverTest {

    @Autowired
    private IKafkaReceiverSRV kafkaReceiverSRV;

    @SpyBean
    private ITransactionEventsSRV eventsSRV;

    @Value("${kafka.statusmanager.topic}")
    String topic;

    @MockBean
    private ConfigSRV config;

    @Test
    @Description("Generic error test on status manager listener - wrong json")
    void kafkaReceiverErrorTest() {
    	given(config.getExpirationDate()).willReturn(0);
        Map<String, Object> map = new HashMap<>();
        MessageHeaders headers = new MessageHeaders(map);

        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

        records.put(new TopicPartition(topic, 0), new ArrayList<>());
        ConsumerRecord<String, String> errorRecord =
                new ConsumerRecord<>(topic, 0, 0, "key", "value");
        assertThrows(BusinessException.class, ()->kafkaReceiverSRV.listenerGtw(errorRecord, headers));
    }

    @Test
    @Description("Success test on status manager listener - ok")
    void kafkaReceiverSuccessTest() {
        Map<String, Object> map = new HashMap<>();
        MessageHeaders headers = new MessageHeaders(map);

        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

        records.put(new TopicPartition(topic, 0), new ArrayList<>());
        ConsumerRecord<String, String> successRecord =
                new ConsumerRecord<>(topic, 0, 0, "key", "{\"eventDate\":\"2022-07-29T11:33:35.316-0500\"}");

        assertDoesNotThrow(()->kafkaReceiverSRV.listenerGtw(successRecord, headers));
    }
}
