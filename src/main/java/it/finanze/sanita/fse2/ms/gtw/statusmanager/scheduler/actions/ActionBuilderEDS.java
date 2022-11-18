/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.base.IActionStepEDS;

import java.util.LinkedHashMap;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes.OK;

public final class ActionBuilderEDS {

    public static ActionRes execute(LinkedHashMap<String, IActionStepEDS> steps) {
        ActionRes res = OK;
        // Iterate entry-set of map
        for (Map.Entry<String, IActionStepEDS> entry : steps.entrySet()) {
            // Get step
            IActionStepEDS step = entry.getValue();
            // Execute it
            res = step.execute();
            // Exit when not OK
            if (res != OK) break;
        }
        return res;
    }
}
