package org.mate.commons.interaction.action.espresso.assertions;

import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.espresso.ViewAssertion;

import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a "View matches" assertion.
 * You need to provide a ViewMatcher that will be checked in this assertion (e.g., isDisplayed).
 */
public class MatchesAssertion extends EspressoViewAssertion {

    /**
     * The matcher used to assert the view.
     */
    private final EspressoViewMatcher viewMatcherForAssertion;

    public MatchesAssertion(EspressoViewMatcher viewMatcherForAssertion) {
        super(EspressoViewAssertionType.MATCHES);
        this.viewMatcherForAssertion = viewMatcherForAssertion;
    }

    @Override
    public ViewAssertion getViewAssertion() {
        return matches(viewMatcherForAssertion.getViewMatcher());
    }

    @Override
    public String getCode() {
        return String.format("matches(%s)", viewMatcherForAssertion.getCode());
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>(viewMatcherForAssertion.getNeededClassImports());
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>(viewMatcherForAssertion.getNeededStaticImports());
        imports.add("androidx.test.espresso.assertion.ViewAssertions.matches");
        return imports;
    }
}
