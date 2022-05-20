package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.TransactionDetailResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.TransactionsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;

/**
 *
 *	Controller for Transactions' Inspection.
 */
@RequestMapping(path = "/v1/transaction/error")
@Tag(name = "Servizio ispezione transazioni in errore")
public interface IErrorTransactionInspectionCTL {

    @GetMapping("/{transactionID}")
	@Operation(summary = "Recupero dettaglio transazione in errore", description = "Recupera il dettaglio degli eventi e dello stato di una transazione.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionDetailResponseDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Transaction found"),
							@ApiResponse(responseCode = "400", description = "Bad Request"),
							@ApiResponse(responseCode = "404", description = "Transaction not found"),
							@ApiResponse(responseCode = "500", description = "Internal Server Error") })
    TransactionDetailResponseDTO getTransactionDetail(@PathVariable(required = true, name = "transactionID") String transactionID, HttpServletRequest request);



	@GetMapping("/find")
	@Operation(summary = "Recupero dettaglio transazione in errore in base ai filtri forniti", description = "Recupera il dettaglio degli eventi e dello stato di una transazione in base ai filtri di ricerca forniti.")
	@ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TransactionsResponseDTO.class)))
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Transaction found"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "404", description = "Transaction not found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	TransactionsResponseDTO findErrorTransaction( 	@RequestParam(required = false) EventTypeEnum lastEventType,
													@RequestParam(required = false) EventTypeEnum eventType,
													@RequestParam(required = false) String identificativoDoc,
													@RequestParam(required = false) String identificativoPaziente,
													@RequestParam(required = false) String identificativoSottomissione,
													@RequestParam(required = false) Boolean forcePublish,
													@RequestParam(required = false) String startDate,
													@RequestParam(required = false) String endDate,
													HttpServletRequest request);
    
}
