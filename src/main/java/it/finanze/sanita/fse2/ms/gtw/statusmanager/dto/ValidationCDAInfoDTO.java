package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActivityEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class ValidationCDAInfoDTO implements AbstractDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5488396671147827913L;

	@Schema(description = "Identificativo del workflow")
    private String workflowInstanceId;

    @Schema(description = "Attività del gateway")
    private ActivityEnum activity;

    @Schema(description = "Identificativo documento")
    private String identificativoDoc;

    @Schema(description = "Identificativo del paziente al momento della creazione del documento")
    private String identificativoPaziente;

    @Schema(description = "Identificativo sottomissione")
    private String identificativoSottomissione;

}
