 it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions;

/**
 * Generic business exception.
 */
public class BusinessException extends RuntimeException {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 4420700371354323215L;

	/**
	 * Message constructor.
	 * 
	 * @param msg Message to be shown.
	 */
	public BusinessException(final String msg) {
		super(msg);
	}

	/**
	 * Complete constructor.
	 * 
	 * @param msg Message to be shown.
	 * @param e   Exception to be shown.
	 */
	public BusinessException(final String msg, final Exception e) {
		super(msg, e);
	}

	/**
	 * Exception constructor.
	 * 
	 * @param e Exception to be shown.
	 */
	public BusinessException(final Exception e) {
		super(e);
	}

}
