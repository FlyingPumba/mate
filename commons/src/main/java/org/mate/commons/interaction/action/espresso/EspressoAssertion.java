package org.mate.commons.interaction.action.espresso;

import org.mate.commons.interaction.action.espresso.assertions.EspressoViewAssertion;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.utils.CodeProducer;

import java.util.HashSet;
import java.util.Set;

/**
 * An Espresso assertion is an assertion that will be executed using the Espresso testing framework.
 * It is composed of a ViewMatcher (that tells Espresso which is the target view) and a
 * ViewAssertion (that tels Espresso what assertion to check on the target view).
 *
 * Note: do not confuse this class (EspressoAssertion) with the EspressoViewAssertion class.
 * The latter is used for representing the actual Espresso ViewAssertion instances (e.g.,
 * isDisplayed()) and can not be used without having an appropriate ViewMatcher (e.g., on which
 * view to perform the assertion).
 */
public class EspressoAssertion implements CodeProducer {

    /**
     * The actual assertion to check on the target view (e.g., isDisplayed, isClickable, etc.)
     */
    private final EspressoViewMatcher espressoViewMatcher;

    /**
     * The selector to indicate Espresso the target view.
     */
    private final EspressoViewAssertion espressoViewAssertion;

    public EspressoAssertion(EspressoViewMatcher espressoViewMatcher,
                             EspressoViewAssertion espressoViewAssertion) {
        this.espressoViewMatcher = espressoViewMatcher;
        this.espressoViewAssertion = espressoViewAssertion;
    }

    @Override
    public String getCode() {
        String viewMatcherCode = espressoViewMatcher.getCode();
        String viewAssertionCode = espressoViewAssertion.getCode();
        String code = String.format("onView(%s).check(%s)", viewMatcherCode, viewAssertionCode);

        return code;
    }

    @Override
    public Set<String> getNeededClassImports() {
        Set<String> imports = new HashSet<>();

        imports.addAll(espressoViewMatcher.getNeededClassImports());
        imports.addAll(espressoViewAssertion.getNeededClassImports());

        return imports;
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.Espresso.onView");

        imports.addAll(espressoViewMatcher.getNeededStaticImports());
        imports.addAll(espressoViewAssertion.getNeededStaticImports());

        return imports;
    }
}
