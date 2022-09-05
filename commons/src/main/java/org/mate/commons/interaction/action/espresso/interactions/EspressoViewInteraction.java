package org.mate.commons.interaction.action.espresso.interactions;

import static androidx.test.espresso.Espresso.onView;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;

import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;

import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper around the Espresso ViewInteraction class.
 * This class is used to represent the interactions that can be performed on a View.
 *
 * The Espresso ViewInteraction class provides the primary interface for test authors to perform
 * actions or asserts on views.
 * Each interaction is associated with a view identified by a view matcher.
 * All view actions and asserts are performed on the UI thread (thus ensuring sequential execution).
 * The same goes for retrieval of views (this is done to ensure that view state is "fresh" prior
 * to execution of each operation).
 */
public class EspressoViewInteraction extends EspressoInteraction {

    public EspressoViewInteraction(EspressoViewMatcher viewMatcher) {
        super(EspressoInteractionType.VIEW_INTERACTION, viewMatcher);
    }

    /**
     * @return the Espresso ViewInteraction object.
     */
    private ViewInteraction getViewInteraction() {
        return onView(viewMatcher.getViewMatcher());
    }

    @Override
    public String getCode() {
        return String.format("onView(%s)", viewMatcher.getCode());
    }

    @Override
    public ViewInteraction perform(ViewAction... actions) {
        ViewInteraction viewInteraction = getViewInteraction();

        if (rootMatcher != null) {
            viewInteraction.inRoot(rootMatcher);
        }

        return viewInteraction.perform(actions);
    }

    @Override
    public Set<String> getNeededClassImports() {
        HashSet<String> imports = new HashSet<>();

        imports.addAll(viewMatcher.getNeededClassImports());

        return imports;
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();

        imports.add("androidx.test.espresso.Espresso.onView");
        imports.addAll(viewMatcher.getNeededStaticImports());

        return imports;
    }

    public EspressoViewInteraction(Parcel in) {
        super(in, EspressoInteractionType.VIEW_INTERACTION);
    }

    public static final Creator<EspressoViewInteraction> CREATOR = new Creator<EspressoViewInteraction>() {
        @Override
        public EspressoViewInteraction createFromParcel(Parcel source) {
            // We need to use the EspressoViewInteraction.CREATOR here, because we want to make sure
            // to remove the EspressoViewInteraction's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (EspressoViewInteraction) EspressoInteraction.CREATOR.createFromParcel(source);
        }

        @Override
        public EspressoViewInteraction[] newArray(int size) {
            return new EspressoViewInteraction[size];
        }
    };
}
