package org.mate.representation.commands;

import android.os.Debug;
import android.os.RemoteException;

import org.mate.commons.IMATEServiceInterface;
import org.mate.commons.IRepresentationLayerInterface;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.espresso.EspressoAction;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.utils.MATELog;
import org.mate.representation.DeviceInfo;
import org.mate.representation.DynamicTest;
import org.mate.representation.ExplorationInfo;
import org.mate.representation.interaction.ActionExecutor;
import org.mate.representation.interaction.ActionExecutorFactory;
import org.mate.representation.state.espresso.EspressoScreenParser;
import org.mate.representation.state.widget.WidgetScreenParser;
import org.mate.representation.test.BuildConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Handles commands requested by the MATE Service (e.g., fetch current available actions).
 */
public class CommandHandler extends IRepresentationLayerInterface.Stub {

    private EspressoScreenParser espressoScreenParser;

    public CommandHandler() {}

    @Override
    public void ping() throws RemoteException {
        // Do nothing. This method is used to test the connection between the MATE Service and
        // the Representation Layer.
    }

    @Override
    public void exit() throws RemoteException {
        MATELog.log("MATE Representation Layer was asked to exit.");
        DynamicTest.keepRunning = false;
    }

    @Override
    public void waitForDebugger() throws RemoteException {
        MATELog.log("MATE Representation Layer waiting for Debugger to be attached to Android " +
                "Process");
        Debug.waitForDebugger();
    }

    @Override
    public String getTargetPackageName() throws RemoteException {
        return BuildConfig.TARGET_PACKAGE_NAME;
    }

    @Override
    public void setRandomSeed(long seed) throws RemoteException {
        try {
            ExplorationInfo.getInstance().setRandomSeed(seed);
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public int getDisplayWidth() throws RemoteException {
        try {
            return DeviceInfo.getInstance().getDisplayWidth();
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public int getDisplayHeight() throws RemoteException {
        try {
            return DeviceInfo.getInstance().getDisplayHeight();
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public boolean grantRuntimePermission(String permission) throws RemoteException {
        try {
            return DeviceInfo.getInstance().grantRuntimePermission(permission);
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public void disableAnimations() throws RemoteException {
        try {
            DeviceInfo.getInstance().disableAnimations();
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public boolean isCrashDialogPresent() throws RemoteException {
        try {
            return DeviceInfo.getInstance().isCrashDialogPresent();
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public String getTargetPackageFilesDir() throws RemoteException {
        try {
            return DeviceInfo.getInstance().getTargetPackageFilesDir();
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public void sendBroadcastToTracer() throws RemoteException {
        try {
            ExplorationInfo.getInstance().sendBroadcastToTracer();
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public int getTopWindowType() throws RemoteException {
        try {
            if (espressoScreenParser == null) {
                espressoScreenParser = new EspressoScreenParser();
            }

            return espressoScreenParser.getTopWindowType();
        } catch (Exception e) {
            logException(e);
            throw e;
        }
    }

    @Override
    public String getCurrentPackageName() throws RemoteException {
        try {
            return ExplorationInfo.getInstance().getCurrentPackageName();
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public String getCurrentActivityName() throws RemoteException {
        try {
            return ExplorationInfo.getInstance().getCurrentActivityName();
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public List<String> getTargetPackageActivityNames() throws RemoteException {
        try {
            return ExplorationInfo.getInstance().getTargetPackageActivityNames();
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public String executeShellCommand(String command) throws RemoteException {
        try {
            return DeviceInfo.getInstance().executeShellCommand(command);
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    @Override
    public boolean executeAction(Action action) throws RemoteException {
        try {
            if (action == null) {
                MATELog.log_error("Trying to execute null action");
                throw new IllegalStateException("executeAction method on representation layer was " +
                        "called for a null action");
            }

            ActionExecutor executor = ActionExecutorFactory.getExecutor(action);

            try {
                boolean success = executor.perform(action);

                resetScreenParsers();

                return success;
            } catch (Exception e) {
                MATELog.log_error(
                        "An exception occurred executing action on representation layer: " +
                        e.getMessage());

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                MATELog.log_error(sw.toString());

                return false;
            }
        } catch (Exception e){
            logException(e);
            throw e;
        }
    }

    /**
     * Reset screen parsers after executing an action, and thus invalidating the screen
     * information that we had.
     */
    private void resetScreenParsers() {
        if (espressoScreenParser != null) {
            espressoScreenParser = null;
        }
    }

    @Override
    public List<Widget> getCurrentScreenWidgets() throws RemoteException {
        try {
            return new WidgetScreenParser().getWidgets();
        } catch (Exception e) {
            logException(e);
            throw e;
        }
    }

    @Override
    public List<EspressoAction> getCurrentScreenEspressoActions() throws RemoteException {
        try {
            if (espressoScreenParser == null) {
                espressoScreenParser = new EspressoScreenParser();
            }

            return espressoScreenParser.getActions();
        } catch (Exception e) {
            logException(e);
            throw e;
        }
    }

    @Override
    public Map<String, EspressoViewMatcher> getCurrentScreenEspressoMatchers(boolean includeAndroidViews) throws RemoteException {
        try {
            if (espressoScreenParser == null) {
                espressoScreenParser = new EspressoScreenParser();
            }

            return espressoScreenParser.getMatchers(includeAndroidViews);
        } catch (Exception e) {
            logException(e);
            throw e;
        }
    }

    @Override
    public Map<String, Map<String, String>> getUIAttributes() throws RemoteException {
        try {
            if (espressoScreenParser == null) {
                espressoScreenParser = new EspressoScreenParser();
            }

            return espressoScreenParser.getUIAttributes();
        } catch (Exception e) {
            logException(e);
            throw e;
        }
    }

    @Override
    public void setReplayMode() throws RemoteException {
        try {
            ExplorationInfo.getInstance().setReplayMode();
        } catch (Exception e) {
            logException(e);
            throw e;
        }
    }

    @Override
    public void setWidgetBasedActions() throws RemoteException {
        try {
            ExplorationInfo.getInstance().setWidgetBasedActions();
        } catch (Exception e) {
            logException(e);
            throw e;
        }
    }

    public void setMateService(IMATEServiceInterface mateService) {
        // do nothing, for now
    }

    /**
     * Log an exception that happened in the Representation Layour
     * @param e the exception to log.
     */
    private void logException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        MATELog.log_error(String.format("Exception occurred in Representation Layer: %s",
                stackTrace));
    }
}
