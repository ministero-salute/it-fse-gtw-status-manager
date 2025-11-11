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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.ITransactionDataCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TransactionDataCTL implements ITransactionDataCTL {

	@Autowired
	private ITransactionEventsSRV service;

    @Override
    public CallbackTransactionDataResponseDTO postTransactionData(HttpServletRequest request, CallbackTransactionDataRequestDTO callbackTransactionDataRequestDTO) {
        log.info("[START] {}() with arguments {}={}", "postTransactionData", "CallbackTransactionDataRequestDTO", callbackTransactionDataRequestDTO);
        service.saveEvent(callbackTransactionDataRequestDTO);
        return new CallbackTransactionDataResponseDTO(Boolean.TRUE);
    }


}
