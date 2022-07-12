package org.mate.commons.state.espresso;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.test.espresso.Root;

import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;
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

    public @Nullable EspressoViewMatcher getViewMatcher(String uniqueId) {
        return this.windowSummary.getViewMatcher(uniqueId);
    }

    public EspressoViewTree getViewTree() {
        return this.viewTree;
    }

    public @Nullable EspressoRootMatcher getRootMatcher() {
        return this.windowSummary.getRootMatcher();
    }
}
