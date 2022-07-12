package org.mate.utils.assertions;

import android.os.RemoteException;

import org.mate.Registry;
import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.EspressoAssertion;
import org.mate.commons.interaction.action.espresso.assertions.EspressoAssertionsFactory;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;
import org.mate.commons.state.espresso.EspressoScreenSummary;
import org.mate.commons.utils.MATELog;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;
import org.mate.service.MATEService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class TestCaseAssertionsGenerator {

    /**
     * The test case for which to generate assertions.
     */
    private final TestCase testCase;

    /**
     * The UI abstraction layer to use durint test cases re-execution.
     */
    private final UIAbstractionLayer uiAbstractionLayer;

    /**
     * The AUT's package name.
     */
    private final String targetPackageName;

    /**
     * The last Espresso Screen fetched.
     */
    private EspressoScreenSummary lastEspressoScreen;

    public TestCaseAssertionsGenerator(TestCase testCase) {
        this.testCase = testCase;
        this.uiAbstractionLayer = Registry.getUiAbstractionLayer();
        this.targetPackageName = Registry.getPackageName();
    }

    /**
     * Generate assertions for a test case.
     *
     * The procedure is as follows:
     * <ul>
     *     <li> We execute each action in the test case.
     *     <li> Meanwhile, we check the UI state after each action and mantain a dictionary of UI
     * properties that are of interest for us.
     *     <li> If a property changes after executing an action, we add an assertion for that property.
     *</ul>
     *
     * Its worth noting that we can not use the test case's stateSequence field:
     * <ul>
     *     <li> If we get the EspressoActions from a State, we may be missing Views in the UI hierarchy
     * for which we can build a unequivocal ViewMatcher but no action was available.
     *     <li> If we get the Widgets from a State, we may have contradictory information that is not
     * useful for the Espresso framework, due to the ussage of the Accessibility Service (e.g.,
     * incorrect text/hint).
     * </ul>
     *
     * @return a test case with assertions.
     */
    public TestCaseWithAssertions generate() {
        TestCaseWithAssertions testCaseWithAssertions = new TestCaseWithAssertions(testCase);

        uiAbstractionLayer.resetApp();

        List<EspressoAssertion> assertionsBeforeTest = generateAssertions();
        testCaseWithAssertions.setAssertionsBeforeTest(assertionsBeforeTest);

        List<Action> actionSequence = testCase.getActionSequence();

        for (int i = 0, actionSequenceSize = actionSequence.size(); i < actionSequenceSize; i++) {
            Action action = actionSequence.get(i);
            String activity = testCase.getActivityAfterAction(i);

            uiAbstractionLayer.executeAction(action);

            if (!activity.startsWith(targetPackageName)) {
                // this action takes us out of the AUT, so we skip generating assertions for it.
                continue;
            }

            List<EspressoAssertion> assertionsAfterAction = generateAssertions();
            testCaseWithAssertions.setAssertionsAfterAction(i, assertionsAfterAction);
        }

        return testCaseWithAssertions;
    }

    /**
     * Generate assertions for a new Espresso ViewTree.
     * @param uiAttributes the new UI attributes.
     * @param viewMatchers
     * @return a list of assertions.
     */
    private List<EspressoAssertion> generateAssertions() {
        EspressoScreenSummary espressoScreen =
                uiAbstractionLayer.getLastScreenState().getEspressoScreenSummary();

        if (espressoScreen == null) {
            // An error occurred while fetching the information from current screen, skip
            // assertions for this line.
            MATELog.log_debug("Skiping assertions for current screen state: missing information.");
            return new ArrayList<>();
        }

        if (lastEspressoScreen == null) {
            // save the first UI attributes
            lastEspressoScreen = espressoScreen;

            // do not generate any assertion for the first UI attributes.
            return new ArrayList<>();
        }

        List<EspressoAssertion> assertions = new ArrayList<>();

        // has any view in last UI disappeared?
        for (Map.Entry<String, EspressoViewMatcher> entry :
                espressoScreen.getDisappearingViewMatchers(lastEspressoScreen).entrySet()) {

            String viewUniqueId = entry.getKey();
            EspressoViewMatcher viewMatcher = entry.getValue();
            EspressoRootMatcher rootMatcher = lastEspressoScreen.getRootMatcher(viewUniqueId);

            addIfNonNull(assertions, EspressoAssertionsFactory.viewIsGone(viewMatcher, rootMatcher));
        }

        // has any view in new UI appeared?
        for (Map.Entry<String, EspressoViewMatcher> entry :
                espressoScreen.getAppearingViewMatchers(lastEspressoScreen).entrySet()) {

            String viewUniqueId = entry.getKey();
            EspressoViewMatcher viewMatcher = entry.getValue();
            EspressoRootMatcher rootMatcher = espressoScreen.getRootMatcher(viewUniqueId);

            addIfNonNull(assertions, EspressoAssertionsFactory.viewHasAppeared(viewMatcher,
                    rootMatcher, espressoScreen.getUiAttributes(viewUniqueId)));
        }

        // has any view appearing in both last and new UI changed an attribute's value?
        for (Map.Entry<String, EspressoViewMatcher> entry :
                espressoScreen.getCommonViewMatchers(lastEspressoScreen).entrySet()) {

            String viewUniqueId = entry.getKey();
            EspressoViewMatcher viewMatcher = entry.getValue();
            EspressoRootMatcher rootMatcher = espressoScreen.getRootMatcher(viewUniqueId);

            Map<String, String> oldAttributes = lastEspressoScreen.getUiAttributes(viewUniqueId);
            Map<String, String> newAttributes = espressoScreen.getUiAttributes(viewUniqueId);

            if (oldAttributes == null || newAttributes == null) {
                // weird, we found the same matcher in last and new screen, but they have
                // different IDs
                continue;
            }

            for (String attrKey : oldAttributes.keySet()) {
                if (!newAttributes.containsKey(attrKey)) {
                    // weird, a view has more attributes now than before -> skip this attribute.
                    continue;
                }

                String oldValue = oldAttributes.get(attrKey);
                String newValue = newAttributes.get(attrKey);

                if (oldValue == null && newValue != null) {
                    // Null value became non-null
                    addIfNonNull(assertions, EspressoAssertionsFactory.viewHasChanged(viewMatcher,
                            rootMatcher, attrKey, oldValue, newValue));
                } else if (oldValue != null && newValue == null) {
                    // Non-null value became null
                    addIfNonNull(assertions, EspressoAssertionsFactory.viewHasChanged(viewMatcher,
                            rootMatcher, attrKey, oldValue, newValue));
                } else if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                    // an attibute's value has changed
                    addIfNonNull(assertions, EspressoAssertionsFactory.viewHasChanged(viewMatcher,
                            rootMatcher, attrKey, oldValue, newValue));
                }
            }
        }

        // save new screen
        lastEspressoScreen = espressoScreen;

        return assertions;
    }

    /**
     * Add assertion if not null.
     * @param assertions the list of assertions.
     * @param assertion the assertion to add.
     */
    private void addIfNonNull(List<EspressoAssertion> assertions, @Nullable EspressoAssertion assertion) {
        if (assertion != null) {
            assertions.add(assertion);
        }
    }
}
