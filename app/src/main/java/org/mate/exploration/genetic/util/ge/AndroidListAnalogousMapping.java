package org.mate.exploration.genetic.util.ge;

import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.UIAction;
import org.mate.utils.ListUtils;

import java.util.List;

/**
 * An integer sequence to android {@link org.mate.model.TestCase} mapping where each codon is
 * directly mapped to an {@link Action} and the test case is ends when all codons where used to
 * determine an action.
 */
public class AndroidListAnalogousMapping extends AndroidListBasedMapping<Integer> {
    /**
     * The default list analogous mapping
     */
    public AndroidListAnalogousMapping() {
        super(-1);
    }

    @Override
    protected boolean finishTestCase() {
        return activeGenotypeChromosome.getValue().size() == activeGenotypeCurrentCodonIndex;
    }

    @Override
    protected Action selectAction() {
        List<UIAction> executableActions = uiAbstractionLayer.getExecutableActions();
        UIAction selectedAction = ListUtils.wrappedGet(
                executableActions,
                activeGenotypeChromosome.getValue().get(activeGenotypeCurrentCodonIndex));
        activeGenotypeCurrentCodonIndex++;
        return selectedAction;
    }
}
