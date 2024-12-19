/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.base.IActionStepEDS;

import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes.OK;

public final class ActionBuilderEDS {

    private ActionBuilderEDS() {}

    public static ActionRes execute(Map<String, IActionStepEDS> steps) {
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
