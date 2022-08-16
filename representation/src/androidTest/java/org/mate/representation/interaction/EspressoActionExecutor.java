package org.mate.representation.interaction;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.input_generation.Mutation;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.commons.interaction.action.espresso.actions.CloseSoftKeyboardAction;
import org.mate.commons.interaction.action.espresso.actions.ToggleRotationAction;
import org.mate.commons.interaction.action.espresso.view_matchers.base.IsRootViewMatcher;
import org.mate.representation.DeviceInfo;
import org.mate.representation.ExplorationInfo;

/**
 * ActionExecutor class for Espresso actions.
 */
public class EspressoActionExecutor extends ActionExecutor {

    public EspressoActionExecutor() {
        super();

        Mutation.setRandom(ExplorationInfo.getInstance().getRandom());
    }

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    @Override
    public boolean perform(Action action) throws AUTCrashException {
        return executeAction((EspressoAction) action);
    }

    /**
     * Executes an Espresso action.
     *
     * @param action The action to be executed.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    private boolean executeAction(EspressoAction action) throws AUTCrashException {
        boolean success = action.execute();

        if (success && action.getEspressoViewAction() instanceof ToggleRotationAction) {
            // Rotation changed, update our internal status
            DeviceInfo.getInstance().toggleInPortraitMode();
        }

        // Always try to close soft keyboard after the executed Espresso action.
        // The only exception if we have exited the AUT. In that case, we don't care about the
        // soft keyboard since the Test case will finish at this point anyway.
        if (ExplorationInfo.getInstance().getCurrentPackageName().equals(ExplorationInfo.getInstance().getTargetPackageName())) {
            EspressoAction closeSoftKeyboardAction = new EspressoAction(
                    new CloseSoftKeyboardAction(),
                    new IsRootViewMatcher(),
                    null);
            closeSoftKeyboardAction.execute();
        }

        return success;
    }
}
