package org.mate.representation.state.espresso;

import androidx.annotation.NonNull;

import org.mate.commons.interaction.action.espresso.EspressoView;
import org.mate.commons.interaction.action.espresso.actions.BackAction;
import org.mate.commons.interaction.action.espresso.actions.ClearTextAction;
import org.mate.commons.interaction.action.espresso.actions.ClickAction;
import org.mate.commons.interaction.action.espresso.actions.CloseSoftKeyboardAction;
import org.mate.commons.interaction.action.espresso.actions.EnterAction;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.actions.LongClickAction;
import org.mate.commons.interaction.action.espresso.actions.MenuAction;
import org.mate.commons.interaction.action.espresso.actions.PressIMEAction;
import org.mate.commons.interaction.action.espresso.actions.ScrollToAction;
import org.mate.commons.interaction.action.espresso.actions.SwipeDownAction;
import org.mate.commons.interaction.action.espresso.actions.SwipeLeftAction;
import org.mate.commons.interaction.action.espresso.actions.SwipeRightAction;
import org.mate.commons.interaction.action.espresso.actions.SwipeUpAction;
import org.mate.commons.interaction.action.espresso.actions.ToggleRotationAction;
import org.mate.commons.interaction.action.espresso.actions.TypeTextAction;
import org.mate.representation.DeviceInfo;
import org.mate.representation.input_generation.TextDataGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Parses the Espresso ViewActions available to be executed on a given View.
 */
public class EspressoViewActionsParser {

    private final EspressoView espressoView;

    public EspressoViewActionsParser(EspressoView espressoView) {
        this.espressoView = espressoView;
    }

    /**
     * @return a list of Espresso ViewActions available for the given View.
     */
    public List<EspressoViewAction> parse() {
        List<EspressoViewAction> parsedActions = new ArrayList<>();

        if (espressoView.shouldBeSkipped()) {
            return parsedActions;
        }

        // There are many actions that we can possible perform on a View (e.g., Click, ClearText,
        // etc). To determine if an action is valid for a View, we check that the later matches the
        // constraints imposed by the actual Espresso's ViewAction.
        List<EspressoViewAction> possibleActions = getAvailableActions();

        for (EspressoViewAction action : possibleActions) {
            if (action.isValidForView(espressoView.getView())) {

                if (action instanceof TypeTextAction) {
                    // change empty text for a more interesting one
                    ((TypeTextAction) action).setText(TextDataGenerator.getInstance().
                            generateTextData(espressoView));
                }

                parsedActions.add(action);
            }
        }

        return parsedActions;
    }

    @NonNull
    private List<EspressoViewAction> getAvailableActions() {
        EspressoViewAction[] possibleActions = {
            new BackAction(),
            new ClearTextAction(),
            new ClickAction(),
            // Double click actions are disabled for now, since they tend to lead to flaky tests
            // new DoubleClickAction(),
            new EnterAction(),
            new LongClickAction(),
            new MenuAction(),
            new PressIMEAction(),
            new ScrollToAction(),
            new SwipeDownAction(),
            new SwipeLeftAction(),
            new SwipeRightAction(),
            new SwipeUpAction(),
            // We use empty text for the TypeTextAction until we know if we can use it for this view
            new TypeTextAction(""),
            new ToggleRotationAction(),
        };

        List<EspressoViewAction> result = new ArrayList<>(Arrays.asList(possibleActions));

        if (DeviceInfo.getInstance().isKeyboardOpened()) {
            result.add(new CloseSoftKeyboardAction());
        }

        return result;
    }
}
