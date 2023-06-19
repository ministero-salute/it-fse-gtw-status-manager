/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.DeleteTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.TxLinksDTO;

import java.util.Collections;
import java.util.Date;

public class TestUtility {
    private TestUtility() {}

    public static GetTxResDTO createFirstTxLinksMock() {
        TxLinksDTO txLinksDTO = new TxLinksDTO();
        txLinksDTO.setNext("2");
        txLinksDTO.setPrev("1");
        GetTxResDTO resDTO = new GetTxResDTO();
        resDTO.setLinks(txLinksDTO);
        resDTO.setWif(Collections.singletonList("1"));
        resDTO.setTimestamp(new Date());
        resDTO.setTraceID("traceId");
        resDTO.setSpanID("spanId");
        return resDTO;
    }

    public static GetTxResDTO createLastTxLinksMock() {
        TxLinksDTO txLinksDTO = new TxLinksDTO();
        txLinksDTO.setNext(null);
        txLinksDTO.setPrev("2");
        GetTxResDTO resDTO = new GetTxResDTO();
        resDTO.setLinks(txLinksDTO);
        resDTO.setWif(Collections.singletonList("2"));
        resDTO.setTimestamp(new Date());
        resDTO.setTraceID("traceId");
        resDTO.setSpanID("spanId");
        return resDTO;
    }

    public static DeleteTxResDTO createDeletedTransactionMock() {
        DeleteTxResDTO resDTO = new DeleteTxResDTO();
        resDTO.setTraceID("traceId");
        resDTO.setSpanID("spanId");
        resDTO.setTimestamp(new Date());
        resDTO.setDeletedTransactions(5);
        return resDTO;
    }
}
