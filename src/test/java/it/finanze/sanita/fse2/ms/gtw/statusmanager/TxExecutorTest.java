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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IProcessorClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.DeleteTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.TxLinksDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.SubjectOrganizationEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.OperationException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.impl.TxExecutor;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl.ConfigSRV;

@SpringBootTest
@ActiveProfiles(Constants.Profile.TEST)
class TxExecutorTest {

    @Mock
    private ConfigSRV configSRV;

    @Mock
    private IProcessorClient processorClient;

    @Mock
    ITransactionEventsRepo transactionRepo;

    @InjectMocks
    private TxExecutor txExecutor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    /**
     * @Test
     *       void testProcessRegion_successfulFlow() throws OperationException {
     *       // given
     *       OffsetDateTime ts = OffsetDateTime.now();
     *       String regionCode = SubjectOrganizationEnum.REGIONE_TOSCANA.getCode();
     * 
     *       // mock first page of transactions
     *       GetTxResDTO firstPage = new GetTxResDTO();
     *       firstPage.setWif(List.of("tx1", "tx2"));
     *       TxLinksDTO links = new TxLinksDTO();
     *       links.setNext(null); // no more pages
     *       firstPage.setLinks(links);
     * 
     *       DeleteTxResDTO deleteRes = new DeleteTxResDTO();
     *       deleteRes.setDeletedTransactions(2L);
     * 
     *       when(processorClient.getTransactions(any(), anyInt(), anyInt(),
     *       eq(regionCode))).thenReturn(firstPage);
     *       when(processorClient.deleteTransactions(any(),
     *       eq(regionCode))).thenReturn(deleteRes);
     *       when(transactionRepo.saveEventsFhir(anyList(), any(Date.class),
     *       any(Date.class))).thenReturn(2);
     *       when(configSRV.getExpirationDate()).thenReturn(5);
     * 
     *       // when
     *       // Using reflection to call private method processRegion
     *       var result = invokeProcessRegion(txExecutor,
     *       SubjectOrganizationEnum.REGIONE_TOSCANA, ts);
     * 
     *       // then
     *       assertNotNull(result);
     *       assertNull(result.error());
     *       assertEquals(2L, result.processed());
     *       assertEquals(2L, result.deleted());
     * 
     *       verify(processorClient)
     *       .getTransactions(any(), anyInt(), anyInt(), eq(regionCode));
     *       verify(transactionRepo)
     *       .saveEventsFhir(anyList(), any(Date.class), any(Date.class));
     *       verify(processorClient)
     *       .deleteTransactions(any(), eq(regionCode));
     *       }
     * 
     * @Test
     *       void testProcessRegion_withNextPage() throws OperationException {
     *       OffsetDateTime ts = OffsetDateTime.now();
     *       String regionCode = SubjectOrganizationEnum.REGIONE_TOSCANA.getCode();
     * 
     *       // page 1 with next link
     *       GetTxResDTO firstPage = new GetTxResDTO();
     *       firstPage.setWif(List.of("tx1"));
     *       TxLinksDTO firstLinks = new TxLinksDTO();
     *       firstLinks.setNext("next-url");
     *       firstPage.setLinks(firstLinks);
     * 
     *       // page 2 without next link
     *       GetTxResDTO secondPage = new GetTxResDTO();
     *       secondPage.setWif(List.of("tx2"));
     *       TxLinksDTO secondLinks = new TxLinksDTO();
     *       secondLinks.setNext(null);
     *       secondPage.setLinks(secondLinks);
     * 
     *       DeleteTxResDTO deleteRes = new DeleteTxResDTO();
     *       deleteRes.setDeletedTransactions(1L);
     * 
     *       when(processorClient.getTransactions(any(), any(), any(),
     *       eq(regionCode))).thenReturn(firstPage);
     *       when(processorClient.getTransactions(eq("next-url"),
     *       eq(regionCode))).thenReturn(secondPage);
     *       when(transactionRepo.saveEventsFhir(anyList(), any(Date.class),
     *       any(Date.class))).thenReturn(1);
     *       when(configSRV.getExpirationDate()).thenReturn(7);
     *       when(processorClient.deleteTransactions(any(),
     *       eq(regionCode))).thenReturn(deleteRes);
     * 
     *       var result = invokeProcessRegion(txExecutor,
     *       SubjectOrganizationEnum.REGIONE_TOSCANA, ts);
     * 
     *       assertEquals(2L, result.processed());
     *       assertEquals(1L, result.deleted());
     *       }
     * 
     * @Test
     *       void testProcessRegion_handlesExceptionGracefully() {
     *       OffsetDateTime ts = OffsetDateTime.now();
     *       String regionCode = SubjectOrganizationEnum.REGIONE_TOSCANA.getCode();
     * 
     *       when(processorClient.getTransactions(any(), any(), any(),
     *       eq(regionCode)))
     *       .thenThrow(new RuntimeException("Network error"));
     * 
     *       var result = invokeProcessRegion(txExecutor,
     *       SubjectOrganizationEnum.REGIONE_TOSCANA, ts);
     * 
     *       assertNotNull(result.error());
     *       assertTrue(result.error().getMessage().contains("Network error"));
     *       }
     * 
     * @Test
     *       void testRetryMechanism_successAfterFailure() {
     *       SupplierStub<String> op = new SupplierStub<>();
     *       op.setResults(List.of(new RuntimeException("fail"), "success"));
     * 
     *       String result = invokeRetry(txExecutor, "TEST", op);
     * 
     *       assertEquals("success", result);
     *       assertEquals(2, op.getCalls());
     *       }
     * 
     * @Test
     *       void testRetryMechanism_allFailures_throwException() {
     *       SupplierStub<String> op = new SupplierStub<>();
     *       op.setResults(List.of(new RuntimeException("1"), new
     *       RuntimeException("2"), new RuntimeException("3")));
     * 
     *       assertThrows(RuntimeException.class, () -> invokeRetry(txExecutor,
     *       "FAIL", op));
     *       }
     * 
     *       // ---- Helpers ----
     * 
     *       private TxExecutor.RegionResult invokeProcessRegion(TxExecutor
     *       executor, SubjectOrganizationEnum region,
     *       OffsetDateTime ts) {
     *       try {
     *       var m = TxExecutor.class.getDeclaredMethod("processRegion",
     *       SubjectOrganizationEnum.class,
     *       OffsetDateTime.class);
     *       m.setAccessible(true);
     *       return (TxExecutor.RegionResult) m.invoke(executor, region, ts);
     *       } catch (Exception e) {
     *       throw new RuntimeException(e);
     *       }
     *       }
     * 
     *       private <T> T invokeRetry(TxExecutor executor, String label,
     *       SupplierStub<T> op) {
     *       try {
     *       var m = TxExecutor.class.getDeclaredMethod("retry", String.class,
     *       java.util.function.Supplier.class);
     *       m.setAccessible(true);
     *       return (T) m.invoke(executor, label, op);
     *       } catch (Exception e) {
     *       throw new RuntimeException(e);
     *       }
     *       }
     */
    /**
     * Helper supplier for testing retry logic.
     * 
     * private static class SupplierStub<T> implements
     * java.util.function.Supplier<T> {
     * private List<Object> results;
     * private int calls = 0;
     * 
     * void setResults(List<Object> results) {
     * this.results = results;
     * }
     * 
     * int getCalls() {
     * return calls;
     * }
     * 
     * @Override
     *           public T get() {
     *           Object res = results.get(calls++);
     *           if (res instanceof RuntimeException ex)
     *           throw ex;
     *           return (T) res;
     *           }
     *           }
     */

}
