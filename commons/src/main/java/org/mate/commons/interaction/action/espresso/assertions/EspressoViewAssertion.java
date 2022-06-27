package org.mate.commons.interaction.action.espresso.assertions;

import androidx.test.espresso.ViewAssertion;

import org.mate.commons.utils.AbstractCodeProducer;

public abstract class EspressoViewAssertion extends AbstractCodeProducer {

    /**
     * The type of Espresso ViewAssertion being represented by this instance.
     */
    private final EspressoViewAssertionType type;

    public EspressoViewAssertion(EspressoViewAssertionType type) {
        this.type = type;
    }

    /**
     * @return the type of Espresso ViewAssertion being represented by this instance.
     */
    public EspressoViewAssertionType getType() {
        return type;
    }

    /**
     * Get the actual Espresso's ViewAssertion instance represented by this EspressoViewAssertion.
     */
    public abstract ViewAssertion getViewAssertion();
}
