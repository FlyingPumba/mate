package org.mate.interaction;

import android.os.RemoteException;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import org.mate.MATE;
import org.mate.Properties;
import org.mate.Registry;
import org.mate.exceptions.AUTCrashException;
import org.mate.interaction.action.Action;
import org.mate.interaction.action.ui.PrimitiveAction;
import org.mate.interaction.action.ui.UIAction;
import org.mate.interaction.action.ui.Widget;
import org.mate.interaction.action.ui.WidgetAction;
import org.mate.model.Edge;
import org.mate.model.IGUIModel;
import org.mate.model.fsm.FSMModel;
import org.mate.state.IScreenState;
import org.mate.state.ScreenStateFactory;
import org.mate.state.ScreenStateType;
import org.mate.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.mate.interaction.UIAbstractionLayer.ActionResult.FAILURE_APP_CRASH;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.FAILURE_EMULATOR_CRASH;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.FAILURE_UNKNOWN;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.SUCCESS;
import static org.mate.interaction.UIAbstractionLayer.ActionResult.SUCCESS_OUTBOUND;

// TODO: make singleton
public class UIAbstractionLayer {

    private static final int UiAutomatorDisconnectedRetries = 3;
    private static final String UiAutomatorDisconnectedMessage = "UiAutomation not connected!";
    private String packageName;
    private DeviceMgr deviceMgr;
    private IScreenState lastScreenState;
    private int lastScreenStateNumber = 0;

    private IGUIModel guiModel;

    public UIAbstractionLayer(DeviceMgr deviceMgr, String packageName) {
        this.deviceMgr = deviceMgr;
        this.packageName = packageName;
        // check for any kind of dialogs (permission, crash, ...) initially

        lastScreenState = clearScreen();
        lastScreenState.setId("S" + lastScreenStateNumber);
        lastScreenStateNumber++;
        guiModel = new FSMModel(lastScreenState);
    }

    /**
     * Returns the list of executable widget actions on the current screen.
     *
     * @return Returns the list of executable widget actions.
     */
    public List<UIAction> getExecutableActions() {
        return getLastScreenState().getActions();
    }

    /**
     * Returns the name of the current activity.
     *
     * @return Returns the name of the current activity.
     */
    public String getCurrentActivity() {
        return getLastScreenState().getActivityName();
    }

    static Long startTime = null;
    static int numberEntriesEA = 0;
    public static List<Long> intermediateValuesEA = new ArrayList<>();
    static Long averageEA = null;

    static void evaluateTimeEA(long startTime) {
        long currentTime = System.currentTimeMillis();
        StringBuilder stb = new StringBuilder("Intermediates: ");
        for (Long intermediate : intermediateValuesEA) {
            intermediate = intermediate - intermediateValuesEA.get(0);
            stb.append(intermediate);
            stb.append("ms ");

        }
        stb.append("Duration: ");
        stb.append(currentTime - startTime).append("ms");
        numberEntriesEA++;
        if (averageEA == null) {
            averageEA = currentTime - startTime;
        } else {
            averageEA = ((numberEntriesEA - 1) * averageEA + (currentTime - startTime)) / numberEntriesEA;
        }
        stb.append(" Average: ").append(averageEA).append("ms");
        intermediateValuesEA.clear();
        MATE.log_runtime(stb.toString(), "executeAction()");
    }

    /**
     * Executes the given action. Retries execution the action when the
     * UIAutomator is disconnected for a pre-defined number of times.
     *
     * @param action The action that should be executed.
     * @return Returns the outcome of the execution, e.g. success.
     */
    public ActionResult executeAction(Action action) {
        startTime = System.currentTimeMillis();
        intermediateValuesEA.add(startTime); //1
        boolean retry = true;
        int retryCount = 0;

        /*
         * FIXME: The UIAutomator bug seems to be unresolvable right now.
         *  We have tried to restart the ADB server, but afterwards the
         *   connection is still broken. Fortunately, this bug seems to appear
         *   very rarely recently.
         */
        while (retry) {
            retry = false;
            try {
                return executeActionUnsafe(action);
            } catch (Exception e) {
                if (e instanceof IllegalStateException
                        && e.getMessage().equals(UiAutomatorDisconnectedMessage)
                        && retryCount < UiAutomatorDisconnectedRetries) {
                    retry = true;
                    retryCount += 1;
                    continue;
                }
                Log.e("acc", "", e);
            }
            //evaluateTimeEA(startTime);
        }
        return FAILURE_UNKNOWN;
    }

    /**
     * Executes the given action. As a side effect, the screen state
     * model is updated.
     *
     * @param action The action to be executed.
     * @return Returns the outcome of the execution, e.g. success.
     */
    private ActionResult executeActionUnsafe(Action action) {
        IScreenState state;
        try {
            intermediateValuesEA.add(System.currentTimeMillis()); //2
            deviceMgr.executeAction(action);
            intermediateValuesEA.add(System.currentTimeMillis()); //3
        } catch (AUTCrashException e) {

            MATE.log_acc("CRASH MESSAGE" + e.getMessage());
            deviceMgr.pressHome();

            if (action instanceof PrimitiveAction) {
                return FAILURE_APP_CRASH;
            }

            // update screen state model
            state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
            state = toRecordedScreenState(state);
            guiModel.update(lastScreenState, state, action);
            lastScreenState = state;

            return FAILURE_APP_CRASH;
        }


        if (action instanceof PrimitiveAction) {
            return SUCCESS;
        }
        intermediateValuesEA.add(System.currentTimeMillis()); //4
        state = clearScreen();
        intermediateValuesEA.add(System.currentTimeMillis()); //5

        // TODO: assess if timeout should be added to primitive actions as well
        // check whether there is a progress bar on the screen
        long timeToWait = waitForProgressBar(state);
        // if there is a progress bar
        if (timeToWait > 0) {
            // add 2 sec just to be sure
            timeToWait += 2000;
            // set that the current action needs to wait before new action
            if (action instanceof WidgetAction) {
                WidgetAction wa = (WidgetAction) action;
                wa.setTimeToWait(timeToWait);
            }
            // get a new state
            state = clearScreen();
        }

        // get the package name of the app currently running
        String currentPackageName = state.getPackageName();

        // if current package is null, emulator has crashed/closed
        if (currentPackageName == null) {
            MATE.log_acc("CURRENT PACKAGE: NULL");
            return FAILURE_EMULATOR_CRASH;
            // TODO: what to do when the emulator crashes?
        }

        // update gui model
        state = toRecordedScreenState(state);
        guiModel.update(lastScreenState, state, action);
        lastScreenState = state;
        intermediateValuesEA.add(System.currentTimeMillis()); //6
        // check whether the package of the app currently running is from the app under test
        // if it is not, this causes a restart of the app
        if (!currentPackageName.equals(this.packageName)) {
            MATE.log("current package different from app package: " + currentPackageName);
            evaluateTimeEA(startTime);
            return SUCCESS_OUTBOUND;
        } else {
            evaluateTimeEA(startTime);
            return SUCCESS;
        }
    }

    /**
     * Returns the last recorded screen state.
     *
     * @return Returns the last recorded screen state.
     */
    public IScreenState getLastScreenState() {
        return lastScreenState;
    }

    static int numberEntriesCS = 0;
    public static List<Long> intermediateValuesCS = new ArrayList<>();
    static Long averageCS = null;

    static void evaluateTime(long startTime) {
        long currentTime = System.currentTimeMillis();
        StringBuilder stb = new StringBuilder("Intermediates: ");
        for (Long intermediate : intermediateValuesCS) {
            intermediate = intermediate - intermediateValuesCS.get(0);
            stb.append(intermediate);
            stb.append("ms ");

        }
        stb.append("Duration: ");
        stb.append(currentTime - startTime).append("ms");
        numberEntriesCS++;
        if (averageCS == null) {
            averageCS = currentTime - startTime;
        } else {
            averageCS = ((numberEntriesCS - 1) * averageCS + (currentTime - startTime)) / numberEntriesCS;
        }
        stb.append(" Average: ").append(averageCS).append("ms");
        intermediateValuesCS.clear();
        MATE.log_runtime(stb.toString(), "clearScreen()");
    }

    /**
     * Clears the screen from all sorts of dialogs. In particular, whenever
     * a permission dialog pops up, the permission is granted. If a crash dialog
     * appears, we press 'HOME'. If a google-sign dialog appears, the 'BACK'
     * button is pressed to return to the AUT. Clicks on 'OK' when a build
     * warning pops up.
     */
    public IScreenState clearScreen() {
        long startTime = System.currentTimeMillis();
        intermediateValuesCS.add(startTime); //1
        IScreenState screenState = null;
        boolean change = true;
        boolean retry = true;
        int retryCount = 0;
        intermediateValuesCS.add(System.currentTimeMillis()); // 2
        while (change || retry) {
            retry = false;
            change = false;
            try {
                // check for crash dialog
                UiObject crashDialog1 = deviceMgr.getDevice().findObject(new UiSelector()
                        .packageName("android").textContains("keeps stopping"));
                UiObject crashDialog2 = deviceMgr.getDevice().findObject(new UiSelector()
                        .packageName("android").textContains("has stopped"));
                intermediateValuesCS.add(System.currentTimeMillis()); // 3
                if (crashDialog1.exists() || crashDialog2.exists()) {
                    // TODO: Click 'OK' on crash dialog window rather than 'HOME'?
                    // press 'HOME' button
                    deviceMgr.pressHome();
                    change = true;
                    continue;
                }
                intermediateValuesCS.add(System.currentTimeMillis()); // 4
                // check for outdated build warnings
                screenState = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                intermediateValuesCS.add(System.currentTimeMillis()); // 5
                for (Widget widget : screenState.getWidgets()) {
                    if (widget.getText().equals("This app was built for an older version of Android " +
                            "and may not work properly. Try checking for updates, or contact the developer.")) {
                        for (UIAction action : screenState.getActions()) {
                            if (action instanceof WidgetAction && ((WidgetAction) action).getWidget().getText().equals("OK")) {
                                try {
                                    deviceMgr.executeAction(action);
                                    break;
                                } catch (AUTCrashException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        change = true;
                    }
                }
                if (change) {
                    continue;
                }

                // check for google sign in dialog
                if (screenState.getPackageName().equals("com.google.android.gms")) {
                    // press BACK to return to AUT
                    MATE.log("Google Sign Dialog detected! Returning.");
                    deviceMgr.pressBack();
                    change = true;
                    continue;
                }

                // check for permission dialog (API 25/28 tested)
                if (screenState.getPackageName().equals("com.google.android.packageinstaller")
                        || screenState.getPackageName().equals("com.android.packageinstaller")) {
                    List<UIAction> actions = screenState.getActions();
                    for (UIAction action : actions) {
                        if (action instanceof WidgetAction) {
                            WidgetAction widgetAction = (WidgetAction) action;

                            /*
                             * The resource id for the allow button stays the same for both API 25
                             * and API 28, although the package name differs.
                             */
                            if (widgetAction.getWidget().getResourceID()
                                    .equals("com.android.packageinstaller:id/permission_allow_button")
                                    || widgetAction.getWidget().getText().toLowerCase().equals("allow")) {
                                try {
                                    deviceMgr.executeAction(action);
                                } catch (AUTCrashException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                    change = true;
                    continue;
                }
            } catch (Exception e) {
                if (e instanceof IllegalStateException
                        && e.getMessage().equals(UiAutomatorDisconnectedMessage)
                        && retryCount < UiAutomatorDisconnectedRetries) {
                    retry = true;
                    retryCount += 1;
                    continue;
                }
                Log.e("acc", "", e);
            }
        }
        evaluateTime(startTime);
        return screenState;
    }

    /**
     * Checks whether a progress bar appeared on the screen. If this is the case,
     * waits a certain amount of time that the progress bar can reach completion.
     *
     * @param state The recording of the current screen.
     * @return Returns the amount of time it has waited for completion.
     */
    private long waitForProgressBar(IScreenState state) {

        long ini = new Date().getTime();
        long end = new Date().getTime();
        boolean hadProgressBar = false;
        boolean hasProgressBar = true;

        // wait a certain amount of time (22 seconds at max)
        while (hasProgressBar && (end - ini) < 22000) {

            // check whether a widget represents a progress bar
            hasProgressBar = false;

            for (Widget widget : state.getWidgets()) {
                if (deviceMgr.checkForProgressBar(widget)) {
                    MATE.log("WAITING PROGRESS BAR TO FINISH");
                    hasProgressBar = true;
                    hadProgressBar = true;
                    Utils.sleep(3000);
                    state = ScreenStateFactory.getScreenState(ScreenStateType.ACTION_SCREEN_STATE);
                }
            }
            end = new Date().getTime();
        }
        if (!hadProgressBar)
            return 0;
        return end - ini;
    }

    /**
     * Returns the screen width.
     *
     * @return Returns the screen width in pixels.
     */
    public int getScreenWidth() {
        return deviceMgr.getScreenWidth();
    }

    /**
     * Returns the screen height.
     *
     * @return Returns the screen height in pixels.
     */
    public int getScreenHeight() {
        return deviceMgr.getScreenHeight();
    }

    /**
     * Resets an app, i.e. clearing the app cache and restarting the app.
     */
    public void resetApp() {
        try {
            deviceMgr.getDevice().wakeUp();
        } catch (RemoteException e) {
            MATE.log("Wake up couldn't be performed");
            e.printStackTrace();
        }
        Registry.getEnvironmentManager().setPortraitMode();
        deviceMgr.reinstallApp();
        Utils.sleep(5000);
        deviceMgr.restartApp();
        Utils.sleep(2000);
        IScreenState state = clearScreen();
        if (Properties.WIDGET_BASED_ACTIONS()) {
            lastScreenState = state;
        }
    }

    /**
     * Restarts the app without clearing the app cache.
     */
    public void restartApp() {
        deviceMgr.restartApp();
        Utils.sleep(2000);
        IScreenState state = clearScreen();
        if (Properties.WIDGET_BASED_ACTIONS()) {
            lastScreenState = state;
        }
    }

    /**
     * Returns the edges (a pair of screen states) that is described by the given action.
     *
     * @param action The given action.
     * @return Returns the edges labeled by the given action.
     */
    public Set<Edge> getEdges(Action action) {
        return guiModel.getEdges(action);
    }

    /**
     * Checks whether the given screen state has been recorded earlier. If this is
     * the case, the recorded screen state is returned, otherwise the given state is returned.
     *
     * @param screenState The given screen state.
     * @return Returns the cached screen state, otherwise the given screen state.
     */
    private IScreenState toRecordedScreenState(IScreenState screenState) {
        Set<IScreenState> recordedScreenStates = guiModel.getStates();
        for (IScreenState recordedScreenState : recordedScreenStates) {
            if (recordedScreenState.equals(screenState)) {
                MATE.log_debug("Using cached screen state!");
                /*
                 * NOTE: We should only return the cached screen state if we can ensure
                 * that equals() actually compares the widgets. Otherwise, we can end up with
                 * widget actions that are not applicable on the current screen.
                 */
                return recordedScreenState;
            }
        }
        screenState.setId("S" + lastScreenStateNumber);
        lastScreenStateNumber++;
        return screenState;
    }

    /**
     * Checks whether the last action lead to a new screen state.
     *
     * @return Returns {@code} if a new screen state has been reached,
     * otherwise {@code} false is returned.
     */
    public boolean reachedNewState() {
        return guiModel.reachedNewState();
    }

    /**
     * The possible outcomes of applying an action.
     */
    public enum ActionResult {
        FAILURE_UNKNOWN, FAILURE_EMULATOR_CRASH, FAILURE_APP_CRASH, SUCCESS_NEW_STATE, SUCCESS, SUCCESS_OUTBOUND
    }
}
