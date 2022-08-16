package org.mate.commons.interaction.action.espresso;

import static androidx.test.espresso.Espresso.onView;

import android.os.Parcel;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;
import org.mate.commons.utils.CodeProducer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An Espresso action is an action that will be executed using the Espresso testing framework.
 * It is composed of a ViewMatcher (that tells Espresso which is the target view) and a
 * ViewAction (that tells Espresso what action to perform on the target view).
 *
 * Note: do not confuse this class (EspressoAction) with the EspressoViewAction class.
 * The latter is used for representing the actual Espresso ViewAction instances (e.g., click())
 * and can not be used without having an appropriate ViewMatcher (e.g., on which view to perform
 * the click).
 */
public class EspressoAction extends Action implements CodeProducer {

    /**
     * The actual action to perform on the target view (e.g., click, long click, etc.)
     */
    private final EspressoViewAction espressoViewAction;

    /**
     * The selector to indicate Espresso the target view.
     */
    private final EspressoViewMatcher espressoViewMatcher;

    /**
     * The root matcher to indicate Espresso on which Root to find the target view.
     */
    private @Nullable final EspressoRootMatcher espressoRootMatcher;

    public EspressoAction(EspressoViewAction espressoViewAction,
                          EspressoViewMatcher espressoViewMatcher,
                          @Nullable EspressoRootMatcher espressoRootMatcher) {
        this.espressoViewAction = espressoViewAction;
        this.espressoViewMatcher = espressoViewMatcher;

        // Use a root matcher only if the ViewAction allows it.
        if (espressoViewAction.allowsRootMatcher()) {
            this.espressoRootMatcher = espressoRootMatcher;
        } else {
            this.espressoRootMatcher = null;
        }
    }

    /**
     * @return The EspressoViewAction that will be executed on the target view.
     */
    public EspressoViewAction getEspressoViewAction() {
        return espressoViewAction;
    }

    /**
     * Executes this Espresso action using the Espresso testing framework.
     * Exceptions are cached so that they do not bubble up and crash the Representation Layer
     * module.
     * @return whether the action was executed successfully or not.
     */
    public boolean execute() {
        try {
            Matcher<View> viewMatcher = espressoViewMatcher.getViewMatcher();
            ViewAction viewAction = espressoViewAction.getViewAction();

            ViewInteraction viewInteraction = onView(viewMatcher);

            if (espressoRootMatcher != null) {
                viewInteraction.inRoot(espressoRootMatcher.getRootMatcher());
            }

            viewInteraction.perform(viewAction);
            return true;
        } catch (Exception e) {
            // do nothing
        }

        return false;
    }

    @Override
    public String getCode() {
        String viewMatcherCode = espressoViewMatcher.getCode();
        String viewActionCode = espressoViewAction.getCode();

        String rootMatcherCode = "";
        if (espressoRootMatcher != null) {
            rootMatcherCode = String.format(".inRoot(%s)", espressoRootMatcher.getCode());
        }

        String code = String.format("onView(%s)%s.perform(%s)",
                viewMatcherCode,
                rootMatcherCode, viewActionCode);

        return code;
    }

    @Override
    public Set<String> getNeededClassImports() {
        Set<String> imports = new HashSet<>();

        imports.addAll(espressoViewMatcher.getNeededClassImports());
        imports.addAll(espressoViewAction.getNeededClassImports());

        if (espressoRootMatcher != null) {
            imports.addAll(espressoRootMatcher.getNeededClassImports());
        }

        return imports;
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.Espresso.onView");

        imports.addAll(espressoViewMatcher.getNeededStaticImports());
        imports.addAll(espressoViewAction.getNeededStaticImports());

        if (espressoRootMatcher != null) {
            imports.addAll(espressoRootMatcher.getNeededStaticImports());
        }

        return imports;
    }

    /**
     * Compares two Espresso actions for equality.
     *
     * @param o The object to which we compare.
     * @return Returns {@code true} if both actions are equal,
     * otherwise {@code false} is returned.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            EspressoAction other = (EspressoAction) o;
            return espressoViewAction == other.espressoViewAction && espressoViewMatcher.equals(other.espressoViewMatcher)
                    && Objects.equals(espressoRootMatcher, other.espressoRootMatcher);
        }
    }

    /**
     * Computes the hash code based on attributes used for {@link #equals(Object)}.
     *
     * @return Returns the associated hash code of the Espresso action.
     */
    @Override
    public int hashCode() {
        return Objects.hash(espressoViewAction, espressoViewMatcher, espressoRootMatcher);
    }

    /**
     * The string representation of the Espresso action.
     */
    @NonNull
    @Override
    public String toString() {
        return getCode();
    }

    @NonNull
    @Override
    public String toShortString() {
        return toString();
    }

    @Override
    public int getIntForActionSubClass() {
        return ACTION_SUBCLASS_ESPRESSO;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.espressoViewAction, flags);
        dest.writeParcelable(this.espressoViewMatcher, flags);
        dest.writeParcelable(this.espressoRootMatcher, flags);
    }

    public EspressoAction(Parcel in) {
        super(in);
        this.espressoViewAction = in.readParcelable(EspressoViewAction.class.getClassLoader());
        this.espressoViewMatcher = in.readParcelable(EspressoViewMatcher.class.getClassLoader());
        this.espressoRootMatcher = in.readParcelable(EspressoRootMatcher.class.getClassLoader());
    }

    public static final Creator<EspressoAction> CREATOR = new Creator<EspressoAction>() {
        @Override
        public EspressoAction createFromParcel(Parcel source) {
            // We need to use the Action.CREATOR here, because we want to make sure to remove the
            // ActionSubClass integer from the beginning of Parcel and call the appropriate
            // constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (EspressoAction) Action.CREATOR.createFromParcel(source);
        }

        @Override
        public EspressoAction[] newArray(int size) {
            return new EspressoAction[size];
        }
    };
}
