// IRepresentationLayerInterface.aidl
package org.mate.commons;

import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;

interface IRepresentationLayerInterface {
    // Representation Layer status & config
    void ping();
    void exit();
    void waitForDebugger();
    String getTargetPackageName();
    void setRandomSeed(long seed);
    void setReplayMode();
    void setWidgetBasedActions();

    // General device info & config
    int getDisplayWidth();
    int getDisplayHeight();
    void disableAnimations();
    boolean grantRuntimePermission(String permission);
    boolean isCrashDialogPresent();
    String getTargetPackageFilesDir();

    // Coverage
    void sendBroadcastToTracer();

    // Activities info
    String getCurrentPackageName();
    String getCurrentActivityName();
    List<String> getTargetPackageActivityNames();

    // Execute actions
    String executeShellCommand(String command);
    boolean executeAction(in Action action);

    // Widget actions
    List<Widget> getCurrentScreenWidgets();

    // Espresso support
    List<EspressoAction> getCurrentScreenEspressoActions();
    Map getCurrentScreenEspressoMatchers();

    // UI state
    Map getUIAttributes();
}