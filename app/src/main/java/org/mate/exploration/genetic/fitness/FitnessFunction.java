package org.mate.exploration.genetic.fitness;

/**
 * The list of supported fitness functions in genetic algorithms.
 */
public enum FitnessFunction {

    NUMBER_OF_ACTIVITIES,
    BRANCH_COVERAGE,
    BRANCH_MULTI_OBJECTIVE,
    BRANCH_DISTANCE,
    BRANCH_DISTANCE_MULTI_OBJECTIVE,
    METHOD_COVERAGE,
    LINE_COVERAGE,
    LINE_PERCENTAGE_COVERAGE,
    NUMBER_OF_CRASHES,
    NUMBER_OF_STATES,
    COVERED_SPECIFIC_ACTIVITY,
    TEST_LENGTH,
    BASIC_BLOCK_LINE_COVERAGE,
    BASIC_BLOCK_BRANCH_COVERAGE,
    BASIC_BLOCK_MULTI_OBJECTIVE,
    GENO_TO_PHENO_TYPE;
}
