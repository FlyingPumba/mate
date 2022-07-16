package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.doubleClick;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Double click Espresso action.
 */
public class DoubleClickAction extends EspressoViewAction {
    public DoubleClickAction() {
        super(EspressoViewActionType.DOUBLE_CLICK);
    }

    @Override
    public ViewAction getViewAction() {
        return doubleClick();
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        if (!view.isClickable()) {
            return false;
        }

        return getViewAction().getConstraints().matches(view);
    }

    @Override
    public String getCode() {
        return "doubleClick()";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.doubleClick");
        return imports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected DoubleClickAction(Parcel in) {
        this();
    }

    public static final Creator<DoubleClickAction> CREATOR = new Creator<DoubleClickAction>() {
        @Override
        public DoubleClickAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (DoubleClickAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public DoubleClickAction[] newArray(int size) {
            return new DoubleClickAction[size];
        }
    };
}