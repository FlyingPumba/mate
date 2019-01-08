package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.model.graph.EventEdge;
import org.mate.model.graph.GraphGUIModel;
import org.mate.model.graph.StateGraph;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.utils.Optional;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class TestCaseMergeCrossOverFunction implements ICrossOverFunction<TestCase> {
    @Override
    public IChromosome<TestCase> cross(List<IChromosome<TestCase>> parents) {
        List<Action> l1 = parents.get(0).getValue().getEventSequence();
        List<Action> l2 = parents.get(1).getValue().getEventSequence();
        if (l2.size() < l1.size()) {
            List<Action> tmp = l1;
            l1 = l2;
            l2 = tmp;
        }

        //Randomly select whether final length should be floored or ceiled
        int lengthBias = Randomness.getRnd().nextInt(2);
        int finalSize = (l1.size() + l2.size() + lengthBias) / 2;

        int choice = Randomness.getInRangeStd(l1.size());
        boolean right = choice != l1.size() - 1;
        int d = 0;
        boolean matched = false;
        //traverse the list from chosen start point to the right and to the left in an alternating pattern
        for (int i = 0; i < l1.size(); i++) {
            int idx = choice + d;
            int cc = l2.size() / 2 + (l1.size() + 1) / 2 - idx;
            Optional<Integer> match = findMatch(l1.get(idx), l2, cc);
            if (match.hasValue()) {
                MATE.log_acc("Found match: " + idx + ", " + match.getValue());
                return merge(l1.subList(0, idx + 1), l2.subList(match.getValue(), l2.size()), finalSize);
            }

            if (right) {
                if (d < 0) {
                    d = -d;
                }
                d = d + 1;
                if (choice - d >= 0) {
                    right = false;
                }
            } else {
                if (d > 0) {
                    d = -d;
                } else {
                    d -= 1;
                }
                if (choice - d + 1 < l1.size()) {
                    right = true;
                }
            }
        }
        return parents.get(0);
    }

    private Optional<Integer> findMatch(Action from, List<Action> l, int start) {
        boolean right = start == 0;
        int d = 0;
        StateGraph sg = ((GraphGUIModel) MATE.guiModel).getStateGraph();

        //traverse the list from chosen start point to the left and to the right in an alternating pattern
        for (int i = 0; i < l.size(); i++) {
            int idx = start + d;

            EventEdge e1 = sg.getEdgeByAction(from);
            EventEdge e2 = sg.getEdgeByAction(l.get(idx));

            if (e1.getTarget().getScreenState().equals(e2.getTarget().getScreenState())) {
                return Optional.some(idx);
            }

            if (right) {
                if (d < 0) {
                    d = -d;
                } else {
                    d = d + 1;
                }
                if (start - d - 1>= 0) {
                    right = false;
                }
            } else {
                if (d > 0) {
                    d = -d;
                }
                d -= 1;

                if (start - d < l.size()) {
                    right = true;
                }
            }
        }
        return Optional.none();
    }

    private IChromosome<TestCase> merge(List<Action> l1, List<Action> l2, int finalSize) {
        List<Action> all = new ArrayList<>(l1);
        all.addAll(l2);
        TestCase testCase = TestCase.newInitializedTestCase();
        Chromosome<TestCase> chromosome = new Chromosome<>(testCase);

        int count = 0;
        for (Action action : all) {
            if (count < finalSize) {
                if (MATE.uiAbstractionLayer.getExecutableActions().contains(action)) {
                    testCase.updateTestCase(action, String.valueOf(count));
                    count++;
                }
            } else {
                break;
            }
        }
        for (int i = count; i < finalSize; i++) {
            Action action = Randomness.randomElement(MATE.uiAbstractionLayer.getExecutableActions());
            testCase.updateTestCase(action, String.valueOf(count));
        }

        return chromosome;
    }
}