package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.uiautomator.UiDevice;

import org.hamcrest.Matcher;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Menu key Espresso action.
 */
public class MenuAction extends EspressoViewAction {
    public MenuAction() {
        super(EspressoViewActionType.MENU);
    }

    @Override
    public ViewAction getViewAction() {
        return pressMenu();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        // This action can only be performed on the root view.
        return isRoot().matches(view);
    }

    @Override
    public String getCode() {
        return "pressMenu()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("org.mate.espresso.tests.TestUtils.pressMenu");
        return imports;
    }

    @Override
    public boolean allowsRootMatcher() {
        // This action is independent of the window on which it is performed.
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected MenuAction(Parcel in) {
        this();
    }

    public static final Creator<MenuAction> CREATOR = new Creator<MenuAction>() {
        @Override
        public MenuAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (MenuAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public MenuAction[] newArray(int size) {
            return new MenuAction[size];
        }
    };

    public static ViewAction pressMenu() {
        return new PressMenuViewAction();
    }

    static final class PressMenuViewAction implements ViewAction {

        @Override
        public Matcher<View> getConstraints() {
            return isRoot();
        }

        @Override
        public String getDescription() {
            return "press menu";
        }

        @Override
        public void perform(UiController uiController, View view) {
            UiDevice.getInstance(getInstrumentation()).pressMenu();
        }
    }
}
