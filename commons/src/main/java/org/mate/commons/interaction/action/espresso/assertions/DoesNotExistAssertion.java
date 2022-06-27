package org.mate.commons.interaction.action.espresso.assertions;

import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;

import androidx.test.espresso.ViewAssertion;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a "View does not exist" assertion.
 */
public class DoesNotExistAssertion extends EspressoViewAssertion {

    public DoesNotExistAssertion() {
        super(EspressoViewAssertionType.DOES_NOT_EXIST);
    }

    @Override
    public ViewAssertion getViewAssertion() {
        return doesNotExist();
    }

    @Override
    public String getCode() {
        return "doesNotExist()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.assertion.ViewAssertions.doesNotExist");
        return imports;
    }
}
