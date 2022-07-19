package org.mate.commons.interaction.action.espresso.assertions;

import androidx.annotation.Nullable;

import org.mate.commons.interaction.action.espresso.EspressoAssertion;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.HasFocusMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.IsClickableMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.IsDisplayedMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.IsEnabledMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.IsFocusedMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.IsRootViewMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.IsSelectedMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.WithAlphaMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.WithContentDescriptionMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.WithEffectiveVisibilityMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.WithHintMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.WithTextMatcher;

import java.util.Map;

public class EspressoAssertionsFactory {

    private static final IsRootViewMatcher IS_ROOT_VIEW_MATCHER = new IsRootViewMatcher();

    /**
     * Asserts that a view has disappeared from the screen.
     * @param viewMatcher the view matcher of the view that has disappeared.
     * @return the assertion.
     */
    public static @Nullable EspressoAssertion viewIsGone(EspressoViewMatcher viewMatcher,
                                                         @Nullable EspressoRootMatcher rootMatcher) {
        if (IS_ROOT_VIEW_MATCHER.getCode().equals(viewMatcher.getCode())) {
            // We don't want to assert that the root view is gone
            return null;
        }

        // Never use a root matcher for the DoesNotExist assertion, since the window for that root
        // may be gone, and the assertion will fail when it tries to find the root.
        return new EspressoAssertion(viewMatcher, new DoesNotExistAssertion(), null);
    }

    /**
     * Asserts that a view has appeared on the screen.
     * @param attributes the UI attributes of the view.
     * @param viewMatcher the view matcher of the view that has appeared.
     * @return the assertion.
     */
    public static @Nullable EspressoAssertion viewHasAppeared(
            EspressoViewMatcher viewMatcher,
            @Nullable EspressoRootMatcher rootMatcher,
            Map<String, String> attributes) {

        if (IS_ROOT_VIEW_MATCHER.getCode().equals(viewMatcher.getCode())) {
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

        if (!attributes.containsKey("is_displayed")||
                "false".equals(attributes.get("is_displayed"))) {
            // We don't want to assert that the view has appeared if it is not actually displayed
            return null;
        }

        return new EspressoAssertion(viewMatcher, new MatchesAssertion(new IsDisplayedMatcher()),
                rootMatcher);
    }

    /**
     * Asserts that a view has changed an attribute's value.
     * @param attrKey the attribute's key.
     * @param oldValue the old value of the attribute.
     * @param newValue the new value of the attribute.
     * @param viewMatcher the view matcher of the view that has changed the attribute.
     * @return the assertion.
     */
    public static @Nullable EspressoAssertion viewHasChanged(EspressoViewMatcher viewMatcher,
                                                             @Nullable EspressoRootMatcher rootMatcher,
                                                             String attrKey,
                                                             @Nullable String oldValue,
                                                             @Nullable String newValue) {
        switch (attrKey) {
            case "enabled":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new IsEnabledMatcher(newValue)), rootMatcher);
            case "selected":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new IsSelectedMatcher(newValue)), rootMatcher);
            case "focused":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new IsFocusedMatcher(newValue)), rootMatcher);
            case "hasFocus":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new HasFocusMatcher(newValue)), rootMatcher);
            case "clickable":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new IsClickableMatcher(newValue)), rootMatcher);
            case "text":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new WithTextMatcher(newValue)), rootMatcher);
            case "contentDescription":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new WithContentDescriptionMatcher(newValue)),
                        rootMatcher);
            case "hint":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new WithHintMatcher(newValue)), rootMatcher);
            case "visibility":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new WithEffectiveVisibilityMatcher(newValue)), rootMatcher);
            case "alpha":
                return new EspressoAssertion(viewMatcher,
                        new MatchesAssertion(new WithAlphaMatcher(newValue)), rootMatcher);
            case "is_displayed":
                if ("false".equals(oldValue) && "true".equals(newValue)) {
                    // view has appeared into screen
                    return new EspressoAssertion(viewMatcher,
                            new MatchesAssertion(new IsDisplayedMatcher()), rootMatcher);
                }
                return null;
            default:
                return null;
        }
    }
}
