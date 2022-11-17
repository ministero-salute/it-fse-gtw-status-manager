package it.finanze.sanita.fse2.ms.gtw.statusmanager.client;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.DeleteTxResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx.GetTxResDTO;

import java.util.Date;

public interface IProcessorClient {

    GetTxResDTO getTransactions(Date timestamp, int page, int limit);

    DeleteTxResDTO deleteTransactions(Date timestamp);

}
