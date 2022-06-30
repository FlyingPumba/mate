package org.mate.representation.interaction;

import android.graphics.Rect;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.StaleObjectException;
import androidx.test.uiautomator.UiObject2;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.Action;
import org.mate.commons.interaction.action.ui.PrimitiveAction;
import org.mate.commons.interaction.action.ui.Widget;
import org.mate.commons.utils.MATELog;
import org.mate.representation.DeviceInfo;
import org.mate.representation.ExplorationInfo;
import org.mate.representation.input_generation.TextDataGenerator;
import org.mate.representation.state.widget.WidgetScreenParser;

import java.util.List;
import java.util.Objects;

/**
 * ActionExecutor class for Primitive actions.
 */
public class PrimitiveActionExecutor extends ActionExecutor {

    /**
     * Executes a given action.
     *
     * @param action The action to be executed.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    @Override
    public boolean perform(Action action) throws AUTCrashException {
        return executeAction((PrimitiveAction) action);
    }

    /**
     * Executes a primitive action, e.g. a click on a specific coordinate.
     *
     * @param action The action to be executed.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     * @throws AUTCrashException Thrown when the action causes a crash of the application.
     */
    private boolean executeAction(PrimitiveAction action) throws AUTCrashException {

        boolean success = false;

        switch (action.getActionType()) {
            case CLICK:
                success = handleClick(action);
                break;
            case LONG_CLICK:
                success = device.swipe(action.getX(), action.getY(), action.getX(), action.getY(),
                        120);
                break;
            case SWIPE_DOWN:
                success = device.swipe(action.getX(), action.getY(), action.getX(),
                        action.getY() - 300,
                        15);
                break;
            case SWIPE_UP:
                success = device.swipe(action.getX(), action.getY(), action.getX(),
                        action.getY() + 300,
                        15);
                break;
            case SWIPE_LEFT:
                success = device.swipe(action.getX(), action.getY(), action.getX() + 300,
                        action.getY(),
                        15);
                break;
            case SWIPE_RIGHT:
                success = device.swipe(action.getX(), action.getY(), action.getX() - 300,
                        action.getY(),
                        15);
                break;
            case TYPE_TEXT:
                success = handleEdit(action, false);
                break;
            case CLEAR_TEXT:
                success = handleEdit(action, true);
                break;
            case BACK:
                success = device.pressBack();
                break;
            case MENU:
                success = device.pressMenu();
                break;
            default:
                throw new IllegalArgumentException("Action type " + action.getActionType()
                        + " not implemented for primitive actions.");
        }

        return success;
    }

    /**
     * Performs a click based on the coordinates of the given action.
     *
     * @param action The given primitive action.
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     */
    public boolean handleClick(PrimitiveAction action) {
        return device.click(action.getX(), action.getY());
    }

    /**
     * Inserts a text into a input field based on the given primitive action.
     *
     * @param action The given primitive action.
     * @param clear Whether the text field should be cleared (filled with an empty string).
     * @return Returns {@code true} if the operation succeeded, otherwise {@code false} is returned.
     */
    public boolean handleEdit(PrimitiveAction action, boolean clear) {

        // clicking on the screen should get a focus on the underlying 'widget'
        device.click(action.getX(), action.getY());
        UiObject2 uiElement = device.findObject(By.focused(true));

        if (uiElement != null) {

            Widget widget = null;

            try {
                /*
                 * We resort here to the underlying widget, otherwise we have no idea what type of
                 * text should be generated as input.
                 */
                widget = findWidget(uiElement);
            } catch (StaleObjectException e) {

                MATELog.log_warn("Stale UiObject2!");
                e.printStackTrace();

                /*
                 * Unfortunately, it can happen that the requested ui element gets immediately stale.
                 * The only way to recover from such a situation is to call findObject() another time.
                 */
                uiElement = device.findObject(By.focused(true));
                if (uiElement != null) {
                    widget = findWidget(uiElement);
                }
            }

            if (widget != null && widget.isEditTextType()) {

                // use empty string for clearing
                String textData = "";

                if (!clear) {

                    /*
                     * If we run in replay mode, we should use the recorded text instead of a new text
                     * that is randomly created. Otherwise, we may end up in a different state and
                     * subsequent actions might not show the same behaviour as in the recorded run.
                     */
                    textData = ExplorationInfo.getInstance().isReplayMode() ? action.getText() :
                            Objects.toString(TextDataGenerator.getInstance().generateTextData(
                                    widget, widget.getMaxTextLength()), "");
                }

                MATELog.log_debug("Inserting text: " + textData);

                try {
                    uiElement.setText(textData);

                    // record for possible replaying + findObject() relies on it
                    action.setText(textData);
                    widget.setText(textData);
                } catch (StaleObjectException e) {
                    MATELog.log_warn("Stale UiObject2!");
                    e.printStackTrace();
                } finally {
                    // we need to close the soft keyboard, but only if it is present
                    if (DeviceInfo.getInstance().isKeyboardOpened()) {
                        device.pressBack();
                    }
                }
            }
        }

        return false;
    }

    /**
     * Finds the corresponding widget to the given ui element if possible. This considers the
     * class name, e.g. android.widget.EditText, the boundaries and the resource name for the
     * comparison.
     *
     * @param uiElement The given ui element.
     * @return Returns the corresponding widget or {@code null} if not possible.
     */
    private Widget findWidget(UiObject2 uiElement) {

        // cache attributes to avoid stale object exception
        String className = uiElement.getClassName();
        Rect bounds = uiElement.getVisibleBounds();
        String resourceName = uiElement.getResourceName();

        List<Widget> widgets = new WidgetScreenParser().getWidgets();

        for (Widget widget : widgets) {

            String resourceID = widget.getResourceID().isEmpty() ? null : widget.getResourceID();

            if (widget.getClazz().equals(className)
                    && widget.getBounds().equals(bounds)
                    && Objects.equals(resourceID, resourceName)) {
                return widget;
            }
        }

        return null;
    }
}
