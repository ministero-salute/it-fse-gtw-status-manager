package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 *	DTO used to return transaction detail data.
 */
@Getter
@Setter
public class TransactionDetailResponseDTO extends ResponseDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144366497297675698L;
	
	private final transient TransactionEventsETY transactionInfo;
	
	
	public TransactionDetailResponseDTO() {
		super();
		transactionInfo = null;
	}

	public TransactionDetailResponseDTO(final LogTraceInfoDTO traceInfo, final TransactionEventsETY inTransactionInfo) {
		super(traceInfo);
		transactionInfo = inTransactionInfo;
	}
    
}
