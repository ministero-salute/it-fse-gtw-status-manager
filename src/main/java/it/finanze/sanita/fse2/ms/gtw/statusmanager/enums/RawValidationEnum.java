package it.finanze.sanita.fse2.ms.gtw.statusmanager.enums;

import lombok.Getter;

public enum RawValidationEnum {

	OK("00", "OK"),
	SYNTAX_ERROR("01", "Errore di sintassi"),
	VOCABULARY_ERROR("02", "Errore dovuto alle terminologie utilizzate"),
	SEMANTIC_ERROR("03", "Errore semantico");

	@Getter
	private String code;
	
	@Getter
	private String description;

	private RawValidationEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}


}