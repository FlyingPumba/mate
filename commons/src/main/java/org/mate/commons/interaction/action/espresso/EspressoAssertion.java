package org.mate.commons.interaction.action.espresso;

import androidx.annotation.Nullable;

import org.mate.commons.interaction.action.espresso.assertions.EspressoViewAssertion;
import org.mate.commons.interaction.action.espresso.interactions.EspressoInteraction;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
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
     * The selector to indicate Espresso the target view.
     */
    private final EspressoInteraction espressoInteraction;

    /**
     * The actual assertion to check on the target view (e.g., isDisplayed, isClickable, etc.)
     */
    private final EspressoViewAssertion espressoViewAssertion;

    /**
     * The root matcher to indicate Espresso on which Root to find the target view.
     */
    @Nullable
    private final EspressoRootMatcher espressoRootMatcher;

    public EspressoAssertion(EspressoInteraction espressoInteraction,
                             EspressoViewAssertion espressoViewAssertion,
                             @Nullable EspressoRootMatcher espressoRootMatcher) {
        this.espressoInteraction = espressoInteraction;
        this.espressoViewAssertion = espressoViewAssertion;
        this.espressoRootMatcher = espressoRootMatcher;
    }

    @Override
    public String getCode() {
        String interactionCode = espressoInteraction.getCode();
        String viewAssertionCode = espressoViewAssertion.getCode();

        String rootMatcherCode = "";
        if (espressoRootMatcher != null) {
            rootMatcherCode = String.format(".inRoot(%s)", espressoRootMatcher.getCode());
        }

        String code = String.format("%s%s.check(%s)",
                interactionCode,
                rootMatcherCode, viewAssertionCode);

        return code;
    }

    @Override
    public Set<String> getNeededClassImports() {
        Set<String> imports = new HashSet<>();

        imports.addAll(espressoInteraction.getNeededClassImports());
        imports.addAll(espressoViewAssertion.getNeededClassImports());

        if (espressoRootMatcher != null) {
            imports.addAll(espressoRootMatcher.getNeededClassImports());
        }

        return imports;
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.Espresso.onView");

        imports.addAll(espressoInteraction.getNeededStaticImports());
        imports.addAll(espressoViewAssertion.getNeededStaticImports());

        if (espressoRootMatcher != null) {
            imports.addAll(espressoRootMatcher.getNeededStaticImports());
        }

        return imports;
    }

    @Override
    public String toString() {
        return this.getCode();
    }
}
