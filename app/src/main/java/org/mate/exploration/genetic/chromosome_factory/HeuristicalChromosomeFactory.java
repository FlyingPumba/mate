package org.mate.exploration.genetic.chromosome_factory;

import org.mate.exploration.genetic.chromosome.IChromosome;
import org.mate.interaction.action.ui.UIAction;
import org.mate.model.TestCase;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This factory generates test cases where the individual actions are weighted
 * and selected according to the approach used in Stoat, see section 3.2.
 */
public class HeuristicalChromosomeFactory extends AndroidRandomChromosomeFactory {

    //stores the number of executions of an actions
    private Map<Action, Integer> executionCounter = new HashMap<>();
    //list of already visited widgets
    private List<String> visitedWidgetIds = new ArrayList<>();
    //stores the number of unvisited widgets followed by an action
    private Map<Action, Integer> unvisitedChildWidgetCounter = new HashMap<>();
    //stores a List of actions leading to the widget (contains the id of the widget)
    private Map<String, Set<Action>> actionsPrecedingWidget = new HashMap<>();

    private double alpha, beta, gamma;

    private Action previousAction = null;

    public HeuristicalChromosomeFactory(int maxNumEvents) {
        this( true, maxNumEvents);
    }

    public HeuristicalChromosomeFactory(boolean resetApp, int maxNumEvents) {
        this(resetApp, maxNumEvents, 1, 0.3, 1.5);
    }

    public HeuristicalChromosomeFactory(boolean resetApp, int maxNumEvents, double alpha, double beta, double gamma) {
        super(resetApp, maxNumEvents);
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    @Override
    public IChromosome<TestCase> createChromosome() {
        IChromosome<TestCase> chromosome = super.createChromosome();

        //update unvisitedActions for last selected action
        computeUnvisitedWidgets(uiAbstractionLayer.getExecutableActions());

        previousAction = null;

        return chromosome;
    }

    @Override
    protected Action selectAction() {
        List<UIAction> executableActions = uiAbstractionLayer.getExecutableActions();

        //compute unvisited Actions of previous action (if there is a previous action)
       computeUnvisitedWidgets(executableActions);

        //store all candidates with (same) highest weight in list
        List<UIAction> candidateActions = new ArrayList<>();
        double maxWeight = 0.0;

        for (UIAction action : executableActions) {
            //create list of actions with the highest weight
            double weight = computeExecutionWeight(action);
            if( weight > maxWeight){
                candidateActions = new ArrayList<>();
                candidateActions.add(action);
                maxWeight = weight;
            } else if (weight == maxWeight) {
                candidateActions.add(action);
            }

            if (previousAction != null) {
                //add previously executed action to list of actions preceding an available widget
                String widgetId = action.getActivityName() + "->" + action.getActionType().name();
                if (action instanceof WidgetAction) {
                    widgetId = ((WidgetAction) action).getWidget().getId();
                }
                if (actionsPrecedingWidget.containsKey(widgetId)) {
                    actionsPrecedingWidget.get(widgetId).add(previousAction);
                } else {
                    actionsPrecedingWidget.put(widgetId, new HashSet<>(Collections.singletonList(previousAction)));
                }
            }

        }

        //select random element form candidates
        UIAction selectedAction = Randomness.randomElement(candidateActions);

        String widgetId = selectedAction.getActivityName() + "->" + selectedAction.getActionType().name();

        if (selectedAction instanceof WidgetAction) {
            widgetId = ((WidgetAction) selectedAction).getWidget().getId();
        }

        //update frequency
        if (executionCounter.containsKey(selectedAction)) {
            executionCounter.put(selectedAction, executionCounter.get(selectedAction) + 1);
        } else {
            executionCounter.put(selectedAction, 1);
        }

        if (previousAction != null) {
            //decrease the number of unvisited widgets, because this widget will be visited next
            if (!visitedWidgetIds.contains(widgetId)) {
                for (Action action : actionsPrecedingWidget.get(widgetId)) {
                    if (unvisitedChildWidgetCounter.get(action) > 0) {
                        unvisitedChildWidgetCounter.put(action, unvisitedChildWidgetCounter.get(action) - 1);
                    }
                }
                visitedWidgetIds.add(widgetId);
            }
        }

        previousAction = selectedAction;

        return selectedAction;
    }

    /**
     * Computes the weight according to Stoat of the given action
     *
     * @param action for which the weight should be computed
     * @return  computed weight for given action
     */
    private double computeExecutionWeight(UIAction action) {

        //compute weight for selected event type
        double eventTypeWeight;
        switch (action.getActionType()) {
            case SWIPE_UP:
            case SWIPE_DOWN:
            case SWIPE_LEFT:
            case SWIPE_RIGHT:
            case BACK:
                eventTypeWeight = 0.5;
                break;
            case MENU:
                eventTypeWeight = 2;
                break;
            default:
                eventTypeWeight = 1;
                break;
        }

        int unvisitedChildren;
        if (unvisitedChildWidgetCounter.containsKey(action)) {
            unvisitedChildren = unvisitedChildWidgetCounter.get(action);
        } else {
            //twice highest if unknown
            int max = 0;
            for (Action key : unvisitedChildWidgetCounter.keySet()) {
                int current = unvisitedChildWidgetCounter.get(key);
                max = current > max ? current : max;
            }
            unvisitedChildren = max * 2;
        }

        //add 1 to not divide by zero
        int executionFrequency = (executionCounter.containsKey(action) ? executionCounter.get(action) : 0) + 1;

        return((alpha * eventTypeWeight) + (beta * unvisitedChildren)) / (gamma * executionFrequency);
    }

    /**
     * Computes the number of unvisited widgets of the selected action in the previous selection
     * @param executableActions List of available actions on current screen
     */
    private void computeUnvisitedWidgets(List<UIAction> executableActions) {
        if (previousAction != null) {
            int count = 0;
            for (UIAction action : executableActions) {

                String widgetId = action.getActivityName() + "->" + action.getActionType().name();

                if (action instanceof WidgetAction) {
                    widgetId = ((WidgetAction) action).getWidget().getId();
                }

                if (!visitedWidgetIds.contains(widgetId)) {
                   count++;
                }
            }
            unvisitedChildWidgetCounter.put(previousAction, count);
        }
    }
}
