package org.mate.commons.interaction.action.espresso.assertions;

import java.util.Map;

public class EspressoAssertionsFactory {

    /**
     * Asserts that a view has disappeared from the screen.
     * @param viewUniqueID thew view's unique ID.
     * @param attributes the UI attributes of the view.
     * @return the assertion.
     */
    public static EspressoViewAssertion viewIsGone(String viewUniqueID, Map<String, String> attributes) {
        return null;
    }

    /**
     * Asserts that a view has appeared on the screen.
     * @param viewUniqueID thew view's unique ID.
     * @param attributes the UI attributes of the view.
     * @return the assertion.
     */
    public static EspressoViewAssertion viewHasAppeared(String viewUniqueID, Map<String, String> attributes) {
        return null;
    }

    /**
     * Asserts that a view has changed an attribute's value.
     * @param viewUniqueID thew view's unique ID.
     * @param attrKey the attribute's key.
     * @param oldValue the old value of the attribute.
     * @param newValue the new value of the attribute.
     * @return the assertion.
     */
    public static EspressoViewAssertion viewHasChanged(String viewUniqueID, String attrKey, String oldValue, String newValue) {
        return null;
    }
}
