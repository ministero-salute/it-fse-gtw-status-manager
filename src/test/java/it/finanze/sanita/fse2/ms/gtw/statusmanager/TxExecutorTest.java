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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.HttpMethod.DELETE;

import java.util.ArrayList;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl.ConfigSRV;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.DeleteTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.FhirEvent;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.impl.TxExecutor;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class TxExecutorTest {

    @Autowired
    private TxExecutor txExecutor;

    @MockBean
    private ConfigSRV config;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void init() {
        mongoTemplate.dropCollection(FhirEvent.class);
    }

    @Test
    void executorSuccessTest() {
        GetTxResDTO first = TestUtility.createFirstTxLinksMock();
        GetTxResDTO last = TestUtility.createLastTxLinksMock();
        DeleteTxResDTO deleted = TestUtility.createDeletedTransactionMock();

        Mockito.when(config.getExpirationDate()).thenReturn(5);

        Mockito.when(restTemplate.getForEntity(anyString(), eq(GetTxResDTO.class)))
                .thenReturn(new ResponseEntity<>(first, HttpStatus.OK))
                .thenReturn(new ResponseEntity<>(last, HttpStatus.OK));

        Mockito.when(restTemplate.exchange(anyString(), eq(DELETE), ArgumentMatchers.isNull(), eq(DeleteTxResDTO.class)))
                .thenReturn(new ResponseEntity<>(deleted, HttpStatus.OK));

        Assertions.assertEquals(ActionRes.OK, txExecutor.execute());
    }

    @Test
    void executorFailureTestOnInit() {
        Mockito.when(config.getExpirationDate()).thenReturn(5);
        Mockito.when(restTemplate.getForEntity(anyString(), eq(GetTxResDTO.class)))
                .thenThrow(ResourceAccessException.class);
        Assertions.assertEquals(ActionRes.KO, txExecutor.execute());
    }

    @Test
    void executorFailureTestOnVerify() {
        GetTxResDTO first = TestUtility.createFirstTxLinksMock();
        first.setWif(new ArrayList<>());
        Mockito.when(config.getExpirationDate()).thenReturn(5);
        Mockito.when(restTemplate.getForEntity(anyString(), eq(GetTxResDTO.class)))
                .thenReturn(new ResponseEntity<>(first, HttpStatus.OK));
        Assertions.assertEquals(ActionRes.EMPTY, txExecutor.execute());
    }

    @Test
    void executorFailureTestOnProcess() {
        GetTxResDTO first = TestUtility.createFirstTxLinksMock();

        Mockito.when(config.getExpirationDate()).thenReturn(5);

        Mockito.when(restTemplate.getForEntity(anyString(), eq(GetTxResDTO.class)))
                .thenReturn(new ResponseEntity<>(first, HttpStatus.OK))
                .thenThrow(ResourceAccessException.class);

        Assertions.assertEquals(ActionRes.KO, txExecutor.execute());
    }

    @Test
    void executorFailureTestOnDelete() {
        GetTxResDTO first = TestUtility.createFirstTxLinksMock();
        GetTxResDTO last = TestUtility.createLastTxLinksMock();

        Mockito.when(config.getExpirationDate()).thenReturn(5);

        Mockito.when(restTemplate.getForEntity(anyString(), eq(GetTxResDTO.class)))
                .thenReturn(new ResponseEntity<>(first, HttpStatus.OK))
                .thenReturn(new ResponseEntity<>(last, HttpStatus.OK));

        Mockito.when(restTemplate.exchange(anyString(), eq(DELETE), ArgumentMatchers.isNull(), eq(DeleteTxResDTO.class)))
                .thenThrow(ResourceAccessException.class);

        Assertions.assertEquals(ActionRes.KO, txExecutor.execute());
    }
}
