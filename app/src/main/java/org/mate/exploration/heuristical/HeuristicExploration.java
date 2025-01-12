package org.mate.exploration.heuristical;

import org.mate.MATE;
import org.mate.Registry;
import org.mate.exploration.Algorithm;
import org.mate.exploration.genetic.chromosome_factory.HeuristicalChromosomeFactory;
import org.mate.exploration.genetic.chromosome_factory.IChromosomeFactory;

public class HeuristicExploration implements Algorithm {

    private final IChromosomeFactory heuristicChromosomeFactory;
    private final boolean alwaysReset;

    public HeuristicExploration(int maxNumEvents) {
        this( true, maxNumEvents);
    }

    public HeuristicExploration(boolean alwaysReset, int maxNumEvents) {
        this.alwaysReset = alwaysReset;
        heuristicChromosomeFactory = new HeuristicalChromosomeFactory(alwaysReset, maxNumEvents);
    }

    public void run() {
        if (!alwaysReset) {
            Registry.getUiAbstractionLayer().resetApp();
        }
        for (int i = 0; true; i++) {
            MATE.log_acc("Exploration #" + (i + 1));
            heuristicChromosomeFactory.createChromosome();
            if (!alwaysReset) {
                Registry.getUiAbstractionLayer().restartApp();
            }
        }
    }
}
