package org.mate.commons.interaction.action.espresso.assertions;

import org.mate.commons.interaction.action.espresso.EspressoAssertion;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.IsDisplayedMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.IsRootMatcher;
import org.mate.commons.interaction.action.espresso.matchers.base.WithEffectiveVisibilityMatcher;

import java.util.Map;

import javax.annotation.Nullable;

public class EspressoAssertionsFactory {

    private static final IsRootMatcher isRootMatcher = new IsRootMatcher();

    /**
     * Asserts that a view has disappeared from the screen.
     * @param viewUniqueID thew view's unique ID.
     * @param attributes the UI attributes of the view.
     * @param viewMatcher the view matcher of the view that has disappeared.
     * @return the assertion.
     */
    public static @Nullable EspressoAssertion viewIsGone(String viewUniqueID,
                                               Map<String, String> attributes,
                                               EspressoViewMatcher viewMatcher) {
        if (isRootMatcher.getCode().equals(viewMatcher.getCode())) {
            // We don't want to assert that the root view is gone
            return null;
        }

        return new EspressoAssertion(viewMatcher, new DoesNotExistAssertion());
    }

    /**
     * Asserts that a view has appeared on the screen.
     * @param viewUniqueID thew view's unique ID.
     * @param attributes the UI attributes of the view.
     * @param viewMatcher the view matcher of the view that has appeared.
     * @return the assertion.
     */
    public static @Nullable EspressoAssertion viewHasAppeared(String viewUniqueID,
                                                    Map<String, String> attributes,
                                                    EspressoViewMatcher viewMatcher) {
        if (isRootMatcher.getCode().equals(viewMatcher.getCode())) {
            // We don't want to assert that the root view has appeared
            return null;
        }

        if (!attributes.containsKey("width") || !attributes.containsKey("height") ||
                "0".equals(attributes.get("width")) || "0".equals(attributes.get("height"))) {
            // We don't want to assert that the view has appeared if it has no size
            return null;
        }

        if (!attributes.containsKey("visibility")||
                !"visible".equals(attributes.get("visibility"))) {
            // We don't want to assert that the view has appeared if it is not actually visible
            return null;
        }

        return new EspressoAssertion(viewMatcher, new MatchesAssertion(new IsDisplayedMatcher()));
    }

    /**
     * Asserts that a view has changed an attribute's value.
     * @param viewUniqueID thew view's unique ID.
     * @param attrKey the attribute's key.
     * @param oldValue the old value of the attribute.
     * @param newValue the new value of the attribute.
     * @param viewMatcher the view matcher of the view that has changed the attribute.
     * @return the assertion.
     */
    public static @Nullable EspressoAssertion viewHasChanged(String viewUniqueID, String attrKey,
                                                   @Nullable String oldValue,
                                                   @Nullable String newValue,
                                                   EspressoViewMatcher viewMatcher) {
        switch (attrKey) {
            case "visibility":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new WithEffectiveVisibilityMatcher(newValue)));
        }

        return null;
    }
}
