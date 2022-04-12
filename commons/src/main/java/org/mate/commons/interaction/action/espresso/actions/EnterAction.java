package org.mate.commons.interaction.action.espresso.actions;

import static androidx.test.espresso.action.ViewActions.pressKey;

import android.os.Parcel;
import android.view.KeyEvent;

import androidx.test.espresso.ViewAction;

public class EnterAction extends EspressoViewAction {
    public EnterAction() {
        super(EspressoViewActionType.ENTER);
    }

    @Override
    public ViewAction getViewAction() {
        return pressKey(KeyEvent.KEYCODE_ENTER);
    }

    @Override
    public String getCode() {
        return "pressKey(KeyEvent.KEYCODE_ENTER)";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected EnterAction(Parcel in) {
        this();
    }

    public static final Creator<EnterAction> CREATOR = new Creator<EnterAction>() {
        @Override
        public EnterAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (EnterAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public EnterAction[] newArray(int size) {
            return new EnterAction[size];
        }
    };
}
