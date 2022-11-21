/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.config;

/**
 * 
 *
 * Constants application.
 */
public final class Constants {

	/**
	 *	Path scan.
	 */
	public static final class ComponentScan {
		
		/**
		 * Configuration mongo path.
		 */
		public static final String CONFIG_MONGO = "it.finanze.sanita.fse2.ms.gtw.statusmanager.mongo";
		
		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}
	
	public static final class Collections {

		public static final String TRANSACTION_DATA = "transaction_data";

		private Collections() {

		}
	}
 
	public static final class Profile {

		/**
		 * Test profile.
		 */
		public static final String TEST = "test";

		public static final String TEST_PREFIX = "test_";

		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}

	public static final class Logs {

		public static final String ERR_REP_FHIR_EVENTS = "Unable to insert FHIR events";

		private Logs() {}
	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
