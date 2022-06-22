package org.mate.utils.assertions;

import org.mate.model.TestCase;

public class TestCaseAssertionsGenerator {

    /**
     * Generate assertions for a test case.
     *
     * The procedure is as follows:
     * - We execute each action in the test case.
     * - Meanwhile, we check the UI state after each action and mantain a dictionary of UI
     * properties that are of interest for us.
     * - If a property changes after executing an action, we add an assertion for that property.
     *
     * @param testCase the test case to work on.
     * @return a test case with assertions.
     */
    public static TestCaseWithAssertions generate(TestCase testCase) {
        TestCaseWithAssertions testCaseWithAssertions = new TestCaseWithAssertions(testCase);

        // TODO (Ivan): generate actual assertions while re-executing the test case

        return testCaseWithAssertions;
    }
}
