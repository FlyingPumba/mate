package org.mate.utils.assertions;

import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.EspressoAssertion;
import org.mate.commons.interaction.action.espresso.assertions.EspressoAssertionsFactory;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.utils.MATELog;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;

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
     * The last UI attributes fetched.
     */
    private Map<String, Map<String, String>> lastUIAttributes;

    /**
     * The last Espresso View matchers fetched.
     */
    private Map<String, EspressoViewMatcher> lastViewMatchers;

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
        Map<String, Map<String, String>> uiAttributes =
                uiAbstractionLayer.getLastScreenState().getUIAttributes();
        Map<String, EspressoViewMatcher> viewMatchers =
                uiAbstractionLayer.getLastScreenState().getEspressoViewMatchers(false);

        if (uiAttributes == null || viewMatchers == null) {
            // An error occurred while fetching the information from current screen, skip
            // assertions for this line.
            MATELog.log_debug("Skiping assertions for current screen state: missing information.");
            return new ArrayList<>();
        }

        if (lastUIAttributes == null) {
            // save the first UI attributes
            lastUIAttributes = uiAttributes;
            lastViewMatchers = viewMatchers;

            // do not generate any assertion for the first UI attributes.
            return new ArrayList<>();
        }

        List<EspressoAssertion> assertions = new ArrayList<>();

        // has any view in last UI disappeared?
        for (String viewUniqueID : lastViewMatchers.keySet()) {
            if (!viewMatchers.containsKey(viewUniqueID)) {
                // a view from last UI is gone
                EspressoViewMatcher viewMatcher = lastViewMatchers.get(viewUniqueID);

                addIfNonNull(assertions, EspressoAssertionsFactory.viewIsGone(viewUniqueID,
                        lastUIAttributes.get(viewUniqueID), viewMatcher));
            }
        }

        // has any view in new UI appeared?
        for (String viewUniqueID : viewMatchers.keySet()) {
            if (!lastViewMatchers.containsKey(viewUniqueID)) {
                // a view has appeared on the new UI.
                EspressoViewMatcher viewMatcher = viewMatchers.get(viewUniqueID);

                addIfNonNull(assertions, EspressoAssertionsFactory.viewHasAppeared(viewUniqueID,
                        uiAttributes.get(viewUniqueID), viewMatcher));
            }
        }

        // has any view appearing in both last and new UI changed an attribute's value?
        for (String viewUniqueID : viewMatchers.keySet()) {
            if (!lastViewMatchers.containsKey(viewUniqueID)) {
                continue;
            }

            EspressoViewMatcher viewMatcher = viewMatchers.get(viewUniqueID);

            Map<String, String> oldAttributes = lastUIAttributes.get(viewUniqueID);
            Map<String, String> newAttributes = uiAttributes.get(viewUniqueID);

            for (String attrKey : oldAttributes.keySet()) {
                if (!newAttributes.containsKey(attrKey)) {
                    // weird, a view has more attributes now than before -> skip this attribute.
                    continue;
                }

                String oldValue = oldAttributes.get(attrKey);
                String newValue = newAttributes.get(attrKey);

                if (oldValue == null && newValue != null) {
                    // Null value became non-null
                    addIfNonNull(assertions, EspressoAssertionsFactory.viewHasChanged(viewUniqueID, attrKey,
                            oldValue, newValue, viewMatcher));
                } else if (oldValue != null && newValue == null) {
                    // Non-null value became null
                    addIfNonNull(assertions, EspressoAssertionsFactory.viewHasChanged(viewUniqueID, attrKey,
                            oldValue, newValue, viewMatcher));
                } else if (oldValue != null && newValue != null && !oldValue.equals(newValue)) {
                    // an attibute's value has changed
                    addIfNonNull(assertions, EspressoAssertionsFactory.viewHasChanged(viewUniqueID, attrKey,
                            oldValue, newValue, viewMatcher));
                }
            }
        }

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
