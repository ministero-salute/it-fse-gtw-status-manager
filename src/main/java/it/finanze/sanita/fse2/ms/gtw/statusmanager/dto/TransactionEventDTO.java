package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import lombok.Builder;
import lombok.Getter;

/**
 * Validation event info
 */
@Getter
@Builder
public class TransactionEventDTO implements AbstractDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -5488396014817827913L;

    /**
     * Date of the event
     */
    @Schema(description = "Date of the event")
    private Date date;

    // @Schema(description = "Attività del gateway")
    // private ActivityEnum activity;

    @Schema(description = "Event type")
	private EventTypeEnum eventType;

    @Schema(description = "Identificativo documento")
    private String identificativoDoc;

    @Schema(description = "Identificativo del paziente al momento della creazione del documento")
    private String identificativoPaziente;

    @Schema(description = "Identificativo sottomissione")
    private String identificativoSottomissione;

    @Schema(description = "Ducument publication forced")
    private Boolean forcePublish;

    @Schema(description = "Validation result")
    private ValidationResultEnum validationResult;

    @Schema(description = "Publication result")
    private PublicationResultEnum publicationResult;


    
}
