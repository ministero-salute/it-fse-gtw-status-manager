package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.AbstractDTO;
import lombok.Data;


/**
 * The Class ErrorResponseDTO.
 * 
 * 	Error response.
 */
@Data
public class ErrorResponseDTO implements AbstractDTO {

	/**
	 * Codice.
	 */
	@Schema(description = "Codice di errore")
	private final Integer code;
	
	/**
	 * Messaggio.
	 */
	@Schema(description = "Messaggio di errore")
	private final String message;

}
