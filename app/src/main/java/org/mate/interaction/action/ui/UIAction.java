package org.mate.interaction.action.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mate.interaction.action.Action;

import java.util.Objects;

/**
 * Represents a ui action. This can be either a widget-based action, a primitive action
 * or a simple action like pressing 'BACK'.
 */
public class UIAction extends Action {

    /**
     * The type of action, e.g. CLICK.
     */
    protected final ActionType actionType;

    /**
     * The activity on which the action should be applied.
     */
    protected final String activityName;

    /**
     * Constructs a new ui action with the given action type.
     *
     * @param actionType The type of action, e.g. CLICK.
     */
    public UIAction(ActionType actionType, String activityName) {
        this.actionType = actionType;
        this.activityName = activityName;
    }

    /**
     * Returns the action type.
     *
     * @return Returns the action type associated with the ui action.
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Returns the activity name associated with the action.
     *
     * @return Returns the activity on which the action should be applied.
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * A simple textual representation. This should conform with
     * the analysis framework.
     *
     * @return Returns the string representation.
     */
    @NonNull
    @Override
    public String toString() {
        return "ui action: " + actionType + " on activity: " + activityName;
    }

    /**
     * Another simple text representation used for the {@link org.mate.model.IGUIModel}.
     *
     * @return Returns a simplified string representation.
     */
    @NonNull
    @Override
    public String toShortString() {
        return actionType.name();
    }

    /**
     * Computes the hashcode of the ui action.
     *
     * @return Returns the hash code associated with the ui action.
     */
    @Override
    public int hashCode() {
        return Objects.hash(actionType, activityName);
    }

    /**
     * Compares two ui action for equality.
     *
     * @param o The other ui action to compare against.
     * @return Returns {@code true} if both ui action are equal,
     *          otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            UIAction other = (UIAction) o;
            return actionType == other.actionType &&
                    Objects.equals(activityName, other.activityName);
        }
    }
}
