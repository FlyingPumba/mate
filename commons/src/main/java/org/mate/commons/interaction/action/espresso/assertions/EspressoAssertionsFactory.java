package org.mate.commons.interaction.action.espresso.assertions;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;

import java.util.Map;

import javax.annotation.Nullable;

public class EspressoAssertionsFactory {

    /**
     * Asserts that a view has disappeared from the screen.
     * @param viewUniqueID thew view's unique ID.
     * @param attributes the UI attributes of the view.
     * @param viewMatcher
     * @return the assertion.
     */
    public static EspressoViewAssertion viewIsGone(String viewUniqueID,
                                                   Map<String, String> attributes,
                                                   @Nullable EspressoViewMatcher viewMatcher) {
        return null;
    }

    /**
     * Asserts that a view has appeared on the screen.
     * @param viewUniqueID thew view's unique ID.
     * @param attributes the UI attributes of the view.
     * @param viewMatcher
     * @return the assertion.
     */
    public static EspressoViewAssertion viewHasAppeared(String viewUniqueID,
                                                        Map<String, String> attributes,
                                                        @Nullable EspressoViewMatcher viewMatcher) {
        return null;
    }

    /**
     * Asserts that a view has changed an attribute's value.
     * @param viewUniqueID thew view's unique ID.
     * @param attrKey the attribute's key.
     * @param oldValue the old value of the attribute.
     * @param newValue the new value of the attribute.
     * @param viewMatcher
     * @return the assertion.
     */
    public static EspressoViewAssertion viewHasChanged(String viewUniqueID, String attrKey,
                                                       @Nullable String oldValue,
                                                       @Nullable String newValue,
                                                       @Nullable EspressoViewMatcher viewMatcher) {
        return null;
    }
}
