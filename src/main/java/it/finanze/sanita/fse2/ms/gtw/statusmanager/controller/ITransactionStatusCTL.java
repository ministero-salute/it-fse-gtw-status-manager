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

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataResponseDTO;
import jakarta.validation.Valid;

/**
 * Controller interface for transaction status operations.
 */
@RequestMapping(path = "/v1")
@Tag(name = "Transaction Status Management")
public interface ITransactionStatusCTL {

    @PostMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Save transaction status",
        description = "Saves transaction status to database and notifies Touchpoint Regionale"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Transaction status saved successfully", 
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CallbackTransactionDataResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad Request - Invalid input data"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal Server Error - Failed to save transaction status"
        )
    })
    CallbackTransactionDataResponseDTO saveTransactionStatus(
        @Valid @RequestBody CallbackTransactionDataRequestDTO request
    );

}
