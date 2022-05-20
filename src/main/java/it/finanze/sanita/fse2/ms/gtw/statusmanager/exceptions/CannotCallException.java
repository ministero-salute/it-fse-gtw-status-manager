package it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions;

/**
 *
 *	Exception used to block unauthorized access to services.
 */
public class CannotCallException extends RuntimeException {

	public CannotCallException(String msg) {
		super(msg);
	}
	
}
