/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.SchedulerResDTO;


public interface ISchedulerActionCTL {

		
	@PostMapping("/v1/runStatusScheduler")
	@Operation(summary = "Run status scheduler", description = "Run scheduler.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = SchedulerResDTO.class)))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Scheduler startato", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = void.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)) })
	void runSchedulerAction();

	}
