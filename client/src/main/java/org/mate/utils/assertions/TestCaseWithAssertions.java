package org.mate.utils.assertions;

import org.mate.commons.utils.MATELog;
import org.mate.model.TestCase;
import org.mate.utils.testcase.writer.EspressoTestCaseWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCaseWithAssertions extends TestCase {

    /**
     * Assertions for each line in the original Test Case.
     * The assertions at index 0 are the assertions before the first action of the Test Case, the
     * assertions at index 1 are the assertions before the second action, etc.
     * The assertions at index N+1 are the assertions after the last action.
     */
    Map<Integer, List<String>> assertions = new HashMap<>();

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
        this.crashStackTrace = testCase.getCrashStackTrace();

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
}
