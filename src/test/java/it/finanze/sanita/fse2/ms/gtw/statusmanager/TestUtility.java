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