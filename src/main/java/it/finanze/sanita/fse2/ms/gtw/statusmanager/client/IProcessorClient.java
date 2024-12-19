 it.finanze.sanita.fse2.ms.gtw.statusmanager.client;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.DeleteTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;

import java.time.OffsetDateTime;

public interface IProcessorClient {

    GetTxResDTO getTransactions(OffsetDateTime timestamp, int page, int limit);

    GetTxResDTO getTransactions(String url);

    DeleteTxResDTO deleteTransactions(OffsetDateTime timestamp);

}
