package org.mate.utils.assertions;

import org.mate.Registry;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeNode;
import org.mate.interaction.UIAbstractionLayer;
import org.mate.model.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestCaseAssertionsGenerator {

    /**
     * The test case for which to generate assertions.
     */
    private final TestCase testCase;

    /**
     * The last UI attributes fetched.
     */
    private Map<String, Map<String, String>> lastUIAttributes;

    public TestCaseAssertionsGenerator(TestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Generate assertions for a test case.
     *
     * The procedure is as follows:
     * - We execute each action in the test case.
     * - Meanwhile, we check the UI state after each action and mantain a dictionary of UI
     * properties that are of interest for us.
     * - If a property changes after executing an action, we add an assertion for that property.
     *
     * Its worth noting that we can not use the test case's stateSequence field:
     * - If we get the EspressoActions from a State, we may be missing Views in the UI hierarchy
     * for which we can build a unequivocal ViewMatcher but no action was available.
     * - If we get the Widgets from a State, we may have contradictory information that is not
     * useful for the Espresso framework, due to the ussage of the Accessibility Service (e.g.,
     * incorrect text/hint).
     *
     * @return a test case with assertions.
     */
    public TestCaseWithAssertions generate() {
        TestCaseWithAssertions testCaseWithAssertions = new TestCaseWithAssertions(testCase);

        // TODO (Ivan): generate actual assertions while re-executing the test case


        // Ejecutamos el test o usamos los States guardados?
        // Creo que lo mejor es re-ejecutar el test, e ir obteniendo todas las Views en la
        // pantalla, según Espresso. I.e., obtener el EspressoViewTree.
        // No nos sirve usar las EspressoActions en los States guardados, porque pueden faltar
        // Views para las que sí hay un ViewMatcher unequivoco.
        // Tampoco sirve usar los Widgets en los States guardados, porque las properties pueden
        // estar mal (e.g., por el Accessibility Service).

        UIAbstractionLayer uiAbstractionLayer = Registry.getUiAbstractionLayer();
        uiAbstractionLayer.resetApp();

        Map<String, Map<String, String>> uiAttributes =
                uiAbstractionLayer.getLastScreenState().getUIAttributes();
        List<String> assertionsBeforeTest = generateAssertions(uiAttributes);
        testCaseWithAssertions.setAssertionsBeforeTest(assertionsBeforeTest);

        List<Action> actionSequence = testCase.getActionSequence();
        for (int i = 0, actionSequenceSize = actionSequence.size(); i < actionSequenceSize; i++) {
            Action action = actionSequence.get(i);

            uiAbstractionLayer.executeAction(action);

            uiAttributes = uiAbstractionLayer.getLastScreenState().getUIAttributes();
            List<String> assertionsAfterAction = generateAssertions(uiAttributes);
            testCaseWithAssertions.setAssertionsAfterAction(i, assertionsAfterAction);
        }

        return testCaseWithAssertions;
    }

    /**
     * Generate assertions for a new Espresso ViewTree.
     * @param uiAttributes the new UI attributes.
     * @return a list of assertions.
     */
    private List<String> generateAssertions(Map<String, Map<String, String>> uiAttributes) {
        if (lastUIAttributes == null) {
            // save the first UI attributes
            lastUIAttributes = uiAttributes;

            // do not generate any assertion for the first UI attributes.
            return new ArrayList<>();
        }

        List<String> assertions = new ArrayList<>();

        // has any view in last UI disappeared?
        for (String viewMATEID : lastUIAttributes.keySet()) {
            if (!uiAttributes.containsKey(viewMATEID)) {
                // a view from last UI is gone
                // TODO (Ivan): add assertion
            }
        }

        // has any view in new UI appeared?
        for (String viewMATEID : uiAttributes.keySet()) {
            if (!lastUIAttributes.containsKey(viewMATEID)) {
                // a view that was not in last UI has appeared
                // TODO (Ivan): add assertion
            }
        }

        // has any view appearing in both last and new UI changed an attribute's value?
        for (String viewMATEID : uiAttributes.keySet()) {
            if (!lastUIAttributes.containsKey(viewMATEID)) {
                continue;
            }

            Map<String, String> oldAttributes = lastUIAttributes.get(viewMATEID);
            Map<String, String> newAttributes = uiAttributes.get(viewMATEID);

            for (String attrKey : oldAttributes.keySet()) {
                if (!newAttributes.containsKey(attrKey)) {
                    // weird, a view has more attributes now than before -> skip this attribute.
                    continue;
                }

                String oldValue = oldAttributes.get(attrKey);
                String newValue = newAttributes.get(attrKey);

                if (!oldValue.equals(newValue)) {
                    // an attibute's value has changed
                    // TODO (Ivan): add assertion
                }
            }
        }

        return assertions;
    }
}
