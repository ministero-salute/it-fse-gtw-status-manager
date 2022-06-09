package it.finanze.sanita.fse2.ms.gtw.statusmanager.enums;

import lombok.Getter;

public enum PublicationResultEnum {

	OK("00", "Pubblicazione effettuata correttamente."),
	OK_FORCED("01","Pubblicazione effettuata con forzatura."),
	SECURITY_ERROR("/msg/security", "Errore in fase di verifica della sicurezza"), 
	MINING_CDA_ERROR("/msg/cda-element", "Errore in fase di estrazione del CDA."), 
	CDA_MATCH_ERROR("/msg/cda-element", "Errore in fase di recupero dell'esito della verifica."), 
	FHIR_MAPPING_ERROR("/msg/fhir-mapping", "Errore semantico."),
	PUBLISHING_ERROR("/msg/publishing-error", "Errore semantico."),
	EMPTY_FILE_ERROR("/msg/empty-file", "File vuoto."),
	DOCUMENT_TYPE_ERROR("/msg/document-type", "Il documento non è pdf."),
	SIGNED_VALIDATION_ERROR("/msg/document-type", "Verifica della firma fallita."),
	MANDATORY_ELEMENT_ERROR("/msg/mandatory-element", "Campo obbligatorio non presente.");

	@Getter
	private String type;
	
	@Getter
	private String title;

	private PublicationResultEnum(String inType, String inTitle) {
		type = inType;
		title = inTitle;
	}

}