package it.finanze.sanita.fse2.ms.gtw.statusmanager.enums;

import lombok.Getter;

public enum ValidationResultEnum {

	OK("about:blank", "Ok"),
	MANDATORY_ELEMENT_ERROR("/msg/mandatory-element", "Campo obbligatorio non presente."), 
	EMPTY_FILE_ERROR("/msg/empty-file", "File vuoto."), 
	FILE_GENERIC_ERROR("/msg/file-generic", "Errore in fase di gestione del file."), 
	DOCUMENT_TYPE_ERROR("/msg/document-type", "Il documento non è pdf."), 
	MINING_CDA_ERROR("/msg/mining-cda", "Errore in fase di estrazione del CDA."), 
	SYNTAX_ERROR("/msg/syntax", "Errore di sintassi."),
	SEMANTIC_ERROR("/msg/semantic", "Errore semantico."),
	VOCABULARY_ERROR("/msg/vocabulary", "Errore vocabolario.");

	@Getter
	private String type;
	
	@Getter
	private String title;

	private ValidationResultEnum(String inType, String inTitle) {
		type = inType;
		title = inTitle;
	}


}