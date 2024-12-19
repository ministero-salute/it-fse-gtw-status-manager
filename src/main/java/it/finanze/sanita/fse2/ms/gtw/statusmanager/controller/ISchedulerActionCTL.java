 it.finanze.sanita.fse2.ms.gtw.statusmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;


public interface ISchedulerActionCTL {

		
	@PostMapping("/v1/runStatusScheduler")
	@Operation(summary = "Run status scheduler", description = "Run scheduler.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Scheduler started"),
		@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
	})
	void runSchedulerAction();

}
