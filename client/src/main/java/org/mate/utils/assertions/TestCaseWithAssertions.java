package org.mate.utils.assertions;

import org.mate.Properties;
import org.mate.commons.utils.MATELog;
import org.mate.model.TestCase;
import org.mate.utils.testcase.writer.EspressoTestCaseWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCaseWithAssertions extends TestCase {

    /**
     * Assertions for each line in the original Test Case.
     * If the test case has N actions, then we have N+1 points in which we can add multiple
     * assertions to the test.
     * The assertions at index 0 are the assertions before the first action of the Test Case, the
     * assertions at index 1 are the assertions before the second action, etc.
     * The assertions at index N+1 are the assertions after the last action.
     */
    private Map<Integer, List<String>> assertions = new HashMap<>();

    private TestCaseWithAssertions(String id) {
        super(id);
    }

    /**
     * Construct a test case with empty assertions
     * @param testCase the base Test Case.
     */
    public TestCaseWithAssertions(TestCase testCase) {
        super();

        // Copy all properties from original test case.
        this.id = testCase.getId();
        this.stateSequence.addAll(testCase.getStateSequence());
        this.actionSequence.addAll(testCase.getActionSequence());
        this.activitySequence.addAll(testCase.getActivitySequence());
        this.crashDetected = testCase.hasCrashDetected();
        this.desiredSize = testCase.getDesiredSize();

        if (Properties.RECORD_STACK_TRACE()) {
            this.crashStackTrace = testCase.getCrashStackTrace();
        }

        // Load empty assertions for all indexes
        for (int i = 0; i < this.actionSequence.size() + 1; i++) {
            assertions.put(i, new ArrayList<>());
        }
    }

    @Override
    public void writeAsEspressoTestIfPossible() {
        try {
            EspressoTestCaseWriter espressoTestWriter = new EspressoTestCaseWriter(this);

            espressoTestWriter.setTestCaseName(String.format("%s_with_assertions",
                    espressoTestWriter.getTestCaseName()));

            boolean success = espressoTestWriter.writeToDefaultFolder();
            if (!success) {
                MATELog.log_warn("Unable to write Espresso test case to internal storage");
            }
        } catch (IllegalArgumentException e) {
            // do nothing, EspressoTestCaseWriter is not suitable for this test case.
        } catch (Exception e) {
            MATELog.log_warn("An exception happened while writing Espresso test case to " +
                    "internal storage: " + e.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            MATELog.log_warn(sw.toString());
        }
    }

    /**
     * Save the assertions to be run before the test case begins.
     * @param assertionsBeforeTest a list of assertions.
     */
    public void setAssertionsBeforeTest(List<String> assertionsBeforeTest) {
        this.assertions.put(0, Collections.unmodifiableList(assertionsBeforeTest));
    }

    /**
     * Save the assertions to be run after an action is executed.
     * @param actionIndex the index of the action to execute.
     * @param assertionsAfterAction a list of assertions.
     */
    public void setAssertionsAfterAction(int actionIndex, List<String> assertionsAfterAction) {
        this.assertions.put(actionIndex + 1, Collections.unmodifiableList(assertionsAfterAction));
    }
}
