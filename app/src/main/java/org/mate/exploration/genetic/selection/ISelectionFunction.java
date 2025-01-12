package org.mate.exploration.genetic.selection;

import org.mate.exploration.genetic.crossover.ICrossOverFunction;
import org.mate.exploration.genetic.mutation.IMutationFunction;
import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.exploration.genetic.fitness.IFitnessFunction;

import java.util.List;

/**
 * Interface for selecting {@link IChromosome}s that should later be used for
 * {@link ICrossOverFunction} and {@link IMutationFunction}
 * @param <T> Type wrapped by the chromosome implementation
 */
public interface ISelectionFunction<T> {
    /**
     * Select {@link IChromosome} to use for crossover and mutation
     * @param population pool of candidates for the selection
     * @param fitnessFunctions functions to determine fitness' of each candiate
     * @return selected candidates
     */
    List<IChromosome<T>> select(List<IChromosome<T>> population, List<IFitnessFunction<T>> fitnessFunctions);
}
