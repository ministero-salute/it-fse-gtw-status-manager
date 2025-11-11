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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping(path = "/v1/transactions")
@Tag(name = "Transaction Data")
public interface ITransactionDataCTL {

    @Operation(summary = "Aggiorna stato finale di esecuzione da EDS")
    @PostMapping(value = "/eds/status", produces = { MediaType.APPLICATION_JSON_VALUE })
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    CallbackTransactionDataResponseDTO postTransactionData(HttpServletRequest request,
                                                           @Valid @RequestBody CallbackTransactionDataRequestDTO callbackTransactionDataRequestDTO);
}
