package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class GetTxResDTO {

    private String traceID;
    private String spanID;

    private Date timestamp;

    private List<String> wif;

    private TxLinksDTO links;

}
