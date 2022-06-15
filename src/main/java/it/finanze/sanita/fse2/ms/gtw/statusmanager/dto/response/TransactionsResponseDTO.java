package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response;

import java.util.List;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 *	DTO used to return transactions data.
 */
@Getter
@Setter
public class TransactionsResponseDTO extends ResponseDTO {

    /**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = -2144366499297675698L;
	
	private final List<TransactionEventsETY> transactions;
	
	
	public TransactionsResponseDTO() {
		super();
		transactions = null;
	}

	public TransactionsResponseDTO(final LogTraceInfoDTO traceInfo, final List<TransactionEventsETY> inTransactions) {
		super(traceInfo);
		transactions = inTransactions;
	}
    
}
