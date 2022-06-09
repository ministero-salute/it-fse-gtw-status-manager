package it.finanze.sanita.fse2.ms.gtw.statusmanager.enums;

import lombok.Getter;

public enum ActivityEnum {

	VALIDATION("V"), 
	PRE_PUBLISHING("P");

	@Getter
	private String code;

	private ActivityEnum(String inCode) {
		code = inCode;
	}


}