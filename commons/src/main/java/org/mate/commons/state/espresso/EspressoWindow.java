package org.mate.commons.state.espresso;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.test.espresso.Root;

import org.mate.commons.interaction.action.espresso.interactions.EspressoInteraction;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;

public class EspressoWindow {

    private final EspressoViewTree viewTree;

    private final EspressoWindowSummary windowSummary;

    public EspressoWindow(Activity activity, Root root) {
        this.viewTree = new EspressoViewTree(root, activity.getClass().getName());
        this.windowSummary = new EspressoWindowSummary(viewTree);
    }

    public EspressoWindowSummary getSummary() {
        return this.windowSummary;
    }

    public @Nullable
    EspressoInteraction getInteraction(String uniqueId) {
        return this.windowSummary.getInteraction(uniqueId);
    }

    public EspressoViewTree getViewTree() {
        return this.viewTree;
    }

    public @Nullable EspressoRootMatcher getRootMatcher() {
        return this.windowSummary.getRootMatcher();
    }
}
