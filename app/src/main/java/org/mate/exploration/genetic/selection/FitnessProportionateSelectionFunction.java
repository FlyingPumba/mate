package org.mate.exploration.genetic.selection;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Performs a roulette wheel selection based on the fitness values. Only applicable where a single
 * fitness function is used.
 *
 * @param <T> Refers either to a {@link org.mate.model.TestCase} or {@link org.mate.model.TestSuite}.
 */
public class FitnessProportionateSelectionFunction<T> implements ISelectionFunction<T> {

    /**
     * Performs a roulette wheel selection proportionate to the fitness values. This is an iterative
     * process. Every round, a new roulette wheel is constructed based on a list of chromosomes
     * (initially the entire population). Then, a random number determines the selection of the next
     * chromosome. After that, the chromosome is removed from the list and the next iteration starts.
     * The iteration ends when the list of chromosomes is empty.
     *
     * @param population A pool of candidates for the selection.
     * @param fitnessFunctions The fitness functions. Note, we assume that this method is called
     *                         only with a single fitness function.
     * @return Returns a list of chromosomes based on the order of the roulette wheel selection.
     */
    @Override
    public List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions) {

        final IFitnessFunction<T> fitnessFunction = fitnessFunctions.get(0);
        final boolean maximizing = fitnessFunction.isMaximizing();

        List<IChromosome<T>> selection = new ArrayList<>();
        List<IChromosome<T>> candidates = new LinkedList<>(population);

        for (int i = 0; i < population.size(); i++) {

            /*
            * Constructs the roulette wheel. Each chromosome is assigned a range proportionate
            * to its fitness value. The first chromosome c1 covers the range [0.0,fitness(c1)],
            * the second chromosome c2 the range (fitness(c1),fitness(c2)], and so on.
             */
            double sum = 0.0;

            for (IChromosome<T> chromosome : candidates) {
                double fitness = fitnessFunction.getNormalizedFitness(chromosome);
                sum += maximizing ? fitness : invertFitnessValue(fitness);
            }

            /*
            * The maximal spectrum of the roulette wheel is defined by the range [0.0,sum].
            * Thus, we pick a random number in that spectrum. The candidate that covers the
            * random number represents the selected chromosome.
             */
            double rnd = Randomness.getRandom(0.0, sum);
            IChromosome<T> selected = null;

            double start = 0.0;
            for (IChromosome<T> chromosome : candidates) {
                double fitness = fitnessFunction.getNormalizedFitness(chromosome);
                double end = start + (maximizing ? fitness : invertFitnessValue(fitness));
                if (rnd <= end) {
                    selected = chromosome;
                    break;
                } else {
                    start = end;
                }
            }

            selection.add(selected);

            // remove selected chromosome from roulette wheel
            candidates.remove(selected);
        }

        return selection;
    }

    /**
     * This method inverts a fitness value - used in case of a minimizing fitness function.
     *
     * @param fitnessValue The fitness value to be inverted.
     * @return Returns the inverted fitness value.
     */
    private double invertFitnessValue(final double fitnessValue) {
        return 1.0 - fitnessValue;
    }
}
