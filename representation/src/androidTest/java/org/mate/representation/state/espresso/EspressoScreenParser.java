package org.mate.representation.state.espresso;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.Root;
import androidx.test.espresso.base.ActiveRootLister;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.base.IsRootViewMatcher;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeNode;
import org.mate.commons.state.espresso.EspressoScreen;
import org.mate.commons.state.espresso.EspressoScreenSummary;
import org.mate.commons.state.espresso.EspressoWindow;
import org.mate.commons.utils.MATELog;
import org.mate.representation.DeviceInfo;
import org.mate.representation.ExplorationInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses the Espresso actions on the current screen.
 */
public class EspressoScreenParser {

    /**
     * A list of discovered EspressoActions on the current AUT's screen.
     */
    private List<EspressoAction> espressoActions;

    /**
     * The Instrumentation provided by the DeviceInfo class.
     */
    private final Instrumentation instrumentation;

    /**
     * The parsed Espresso Screen, or null if it was not available.
     */
    private final @Nullable EspressoScreen espressoScreen;

    public EspressoScreenParser() {
        instrumentation = DeviceInfo.getInstance().getInstrumentation();
        espressoScreen = buildEspressoScreen();
    }

    /**
     * @return A list of discovered EspressoActions on the current AUT's screen.
     */
    public @Nullable List<EspressoAction> getActions() {
        if (espressoScreen == null) {
            MATELog.log_debug("No Espresso screen found, unable to get actions");
            return null;
        }

        if (espressoActions == null) {
            parseEspressoActions();

            for (EspressoAction action : espressoActions) {
                MATELog.log("Espresso action: " + action.getCode());
            }
        }

        return espressoActions;
    }

    /**
     * Parses the Espresso actions available in the top window of current Espresso Screen.
     * An Espresso action is found when we find a View for which we can execute a ViewAction, and we
     * also find a ViewMatcher that unequivocally targets it.
     *
     * This method relies on the EspressoViewActionsParser class to determine which ViewActions can
     * be executed on each View. If a View has no suitable actions, we skip the phase of finding a
     * unequivocal ViewMatcher all together.
     *
     * If a View does not have a unequivocal matcher combination, it is skipped as well.
     */
    private void parseEspressoActions() {
        long startTime = System.nanoTime();

        if (espressoActions == null) {
            espressoActions = new ArrayList<>();
        }

        EspressoWindow topWindow = this.espressoScreen.getTopWindow();

        for (EspressoViewTreeNode node : topWindow.getViewTree().getAllNodes()) {

            EspressoViewActionsParser viewActionsParser =
                    new EspressoViewActionsParser(node.getEspressoView());
            List<EspressoViewAction> espressoViewActions = viewActionsParser.parse();

            if (espressoViewActions.size() == 0) {
                // nothing to do on this view, skip View Matcher generation
                continue;
            }

            EspressoViewMatcher espressoViewMatcher =
                    this.espressoScreen.getViewMatcherInScreenForUniqueId(node.getEspressoView().getUniqueId());
            if (espressoViewMatcher == null) {
                // we weren't able to generate a unequivocal matcher combination for this view, skip
                // it.
                continue;
            }

            EspressoRootMatcher rootMatcher = espressoScreen.getSummary().getRootMatcher(node.getEspressoView().getUniqueId());

            // Create and save the EspressoAction instances
            for (EspressoViewAction espressoViewAction : espressoViewActions) {
                EspressoAction espressoAction = new EspressoAction(
                        espressoViewAction,
                        espressoViewMatcher,
                        rootMatcher);
                espressoActions.add(espressoAction);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get ms.
        MATELog.log_debug(String.format("Loading of Espresso actions took %d ms", duration));
    }

    /**
     * Builds an Espresso Screen with the roots of the current Activity in Resumed state.
     *
     * This method's implementation is inspired by code from the Android test open source project.
     * In particular, we will use reflection to access the {@link androidx.test.espresso.base.RootsOracle#listActiveRoots}
     * method. We need to use reflection because the RootsOracle class is private in the androidx
     * testing library.
     * URL: https://github.com/android/android-test/blob/master/espresso/core/java/androidx/test/espresso/base/RootsOracle.java
     *
     * Below is the documentation of the RootsOracle class, for context:
     *
     * Provides access to all root views in an application.
     *
     * <p>95% of the time this is unnecessary and we can operate solely on current Activity's
     * root view as indicated by getWindow().getDecorView(). However in the case of popup
     * windows, menus, and dialogs the actual view hierarchy we should be operating on is not
     * accessible through public apis.
     *
     * <p>In the spirit of degrading gracefully when new api levels break compatibility, callers
     * handle a list of size 0 by assuming getWindow().getDecorView() on the currently resumed
     * is the sole root - this assumption will be correct often enough.
     *
     * <p>Obviously, you need to be on the main thread to use this.
     *
     * @return A Espresso Screen or null if unavailable.
     */
    private @Nullable EspressoScreen buildEspressoScreen() {
        final EspressoScreen[] espressoScreen = {null};

        instrumentation.runOnMainSync(() -> {
            ArrayList<Activity> resumedActivities = new ArrayList<>(
                    ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED));

            if (resumedActivities.size() == 0) {
                // No activity is found in resumed state, we probably left the AUT.
                MATELog.log_debug("No resumed activities found, unable to get root view");
                return;
            }

            Activity activity = resumedActivities.get(0);

            if (!ExplorationInfo.getInstance().getTargetPackageName().equals(
                    activity.getPackageName())) {
                // The resumed activity is for a different package name than the one we are
                // targeting. Exit this function as if we haven't found a root view.
                MATELog.log_debug("Resumed activity is for a different package");
                return;
            }

            List<Root> roots;
            try {
                // Create a new instance of the RootsOracle class
                Class rootsOracleClass = Class.forName("androidx.test.espresso.base.RootsOracle");
                Constructor<ActiveRootLister> rootsOracleConstructor = rootsOracleClass.getDeclaredConstructor(new Class[]{Looper.class});
                rootsOracleConstructor.setAccessible(true);
                ActiveRootLister sRootsOracle = rootsOracleConstructor.newInstance(Looper.getMainLooper());

                // Call the method listActiveRoots
                Method sRootsOracle_listActiveRoots = rootsOracleClass.getMethod("listActiveRoots");
                roots = (List) sRootsOracle_listActiveRoots.invoke(sRootsOracle);
            } catch (Exception e) {
                MATELog.log_error("A problem occurred listing roots: " + e.getMessage());
                throw new RuntimeException("A problem occurred listing roots", e);
            }

            if (roots != null && roots.size() > 0) {
                espressoScreen[0] = new EspressoScreen(activity, roots);
            } else {
                MATELog.log_error("Roots oracle returned no roots");
            }
        });

        if (espressoScreen[0] == null) {
            MATELog.log_error("Unable to find roots on a resumed activity for building " +
                    "EspressoScreen");
            return null;
        }

        return espressoScreen[0];
    }

    public @Nullable EspressoScreenSummary getEspressoScreenSummary() {
        if (espressoScreen == null) {
            return null;
        }

        return espressoScreen.getSummary();
    }
}
