 it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.actions.base;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActionRes;

@FunctionalInterface
public interface IActionStepEDS {
    ActionRes execute();
}
