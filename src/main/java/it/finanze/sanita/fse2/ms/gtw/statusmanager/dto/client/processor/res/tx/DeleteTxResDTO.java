/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.processor.res.tx;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DeleteTxResDTO {

    private String traceID;
    
    private String spanID;

    private Date timestamp;

    private long deletedTransactions;
}
