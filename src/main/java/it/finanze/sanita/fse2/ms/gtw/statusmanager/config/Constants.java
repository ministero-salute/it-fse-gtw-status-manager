package it.finanze.sanita.fse2.ms.gtw.statusmanager.config;

/**
 * 
 * @author vincenzoingenito
 *
 * Constants application.
 */
public final class Constants {

	/**
	 *	Path scan.
	 */
	public static final class ComponentScan {

		/**
		 * Base path.
		 */
		public static final String BASE = "it.sanita.statusmanager";

		/**
		 * Controller path.
		 */
		public static final String CONTROLLER = "it.sanita.statusmanager.controller";

		/**
		 * Service path.
		 */
		public static final String SERVICE = "it.sanita.statusmanager.service";

		/**
		 * Configuration path.
		 */
		public static final String CONFIG = "it.sanita.statusmanager.config";
		
		/**
		 * Configuration mongo path.
		 */
		public static final String CONFIG_MONGO = "it.sanita.statusmanager.mongo";
		
		/**
		 * Configuration mongo repository path.
		 */
		public static final String REPOSITORY_MONGO = "it.sanita.statusmanager.repository";
		 
		
		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}
 
	public static final class Profile {

		/**
		 * Test profile.
		 */
		public static final String TEST = "test";

		/**
		 * Dev profile.
		 */
		public static final String DEV = "dev";

		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}
  
	/**
	 *	Constants.
	 */
	private Constants() {

	}

}
