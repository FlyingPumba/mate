package org.mate.exploration.genetic.fitness;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.model.TestCase;
import org.mate.model.TestSuite;

public class AmountCrashesFitnessFunction implements IFitnessFunction<TestSuite> {

    @Override
    public double getFitness(IChromosome<TestSuite> chromosome) {
        int amountCrashes = 0;
        for (TestCase testCase : chromosome.getValue().getTestCases()) {
            amountCrashes += testCase.getCrashDetected() ? 1 : 0;
        }
        return amountCrashes;
    }

    @Override
    public boolean isMaximizing() {
        return true;
    }

    @Override
    public double getNormalizedFitness(IChromosome<TestSuite> chromosome) {
        return getFitness(chromosome) / chromosome.getValue().getTestCases().size();
    }
}
