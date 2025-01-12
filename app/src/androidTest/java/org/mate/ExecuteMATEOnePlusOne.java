package org.mate;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mate.exploration.genetic.algorithm.Algorithm;
import org.mate.exploration.genetic.builder.GeneticAlgorithmBuilder;
import org.mate.exploration.genetic.chromosome_factory.ChromosomeFactory;
import org.mate.exploration.genetic.core.IGeneticAlgorithm;
import org.mate.exploration.genetic.fitness.FitnessFunction;
import org.mate.exploration.genetic.mutation.MutationFunction;
import org.mate.exploration.genetic.selection.SelectionFunction;
import org.mate.exploration.genetic.termination.TerminationCondition;
import org.mate.model.TestCase;


@RunWith(AndroidJUnit4.class)
public class ExecuteMATEOnePlusOne {


    @Test
    public void useAppContext() {

        MATE.log_acc("Starting Evolutionary Search...");
        MATE.log_acc("One-plus-one (new) algorithm");

        MATE mate = new MATE();

        final IGeneticAlgorithm<TestCase> onePlusOne = new GeneticAlgorithmBuilder()
                .withAlgorithm(Algorithm.ONE_PLUS_ONE)
                .withChromosomeFactory(ChromosomeFactory.ANDROID_RANDOM_CHROMOSOME_FACTORY)
                .withSelectionFunction(SelectionFunction.FITNESS_SELECTION)
                .withMutationFunction(MutationFunction.TEST_CASE_CUT_POINT_MUTATION)
                .withFitnessFunction(FitnessFunction.BRANCH_DISTANCE)
                .withTerminationCondition(TerminationCondition.CONDITIONAL_TERMINATION)
                .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
                .build();

        mate.testApp(onePlusOne);

        //Report
        //Vector<TestCase> ts = new Vector<>(OnePlusOne.testsuite.values());
        //MATE.log_acc("Final Report: test cases number = "+ts.size());

        //MATE.log_acc(OnePlusOne.coverageArchive.keySet().toString());
        //MATE.log_acc("Visited GUI States number = "+ OnePlusOne.coverageArchive.keySet().size());
        //MATE.log_acc("Covered GUI States = "+ OnePlusOne.testsuite.get("0").getVisitedStates().size());
    }
}
