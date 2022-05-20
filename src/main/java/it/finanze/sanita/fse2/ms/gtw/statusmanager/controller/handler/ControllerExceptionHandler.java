package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import brave.Tracer;
import io.micrometer.core.instrument.config.validate.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.CannotCallException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.EntityNotFoundException;

/**
 * 
 * @author CPIERASC
 *
 *	Controller exception handler.
 */
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {


	/**
	 * Tracker log.
	 */
	@Autowired
	private Tracer tracer;

    @ExceptionHandler(value = {EntityNotFoundException.class})
    protected ResponseEntity<ResponseDTO> handleEntityNotFoundException(final Exception ex, final WebRequest request) {
    	return handleException(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {CannotCallException.class})
    protected ResponseEntity<ResponseDTO> handleCannotCallException(final Exception ex, final WebRequest request) {
    	return handleException(ex, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<ResponseDTO> handleValidationException(final Exception ex, final WebRequest request) {
    	return handleException(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ResponseDTO> handleGenericException(final Exception ex, final WebRequest request) {
    	return handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

	private ResponseEntity<ResponseDTO> handleException(final Exception ex, final HttpStatus status) {
		final ResponseDTO out = new ResponseDTO(getLogTraceInfo(), status.value(), ex.getMessage());
        return new ResponseEntity<>(out, new HttpHeaders(), status);
	}

	private LogTraceInfoDTO getLogTraceInfo() {
		return new LogTraceInfoDTO(
				tracer.currentSpan().context().spanIdString(), 
				tracer.currentSpan().context().traceIdString());
	}

}