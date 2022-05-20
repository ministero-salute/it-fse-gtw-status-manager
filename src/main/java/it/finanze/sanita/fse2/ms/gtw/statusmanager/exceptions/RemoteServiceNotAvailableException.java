package it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions;

public class RemoteServiceNotAvailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RemoteServiceNotAvailableException(final String msg) {
		super(msg);
	}

	public RemoteServiceNotAvailableException(final String msg, final Exception ex) {
		super(msg, ex);
	}
}
