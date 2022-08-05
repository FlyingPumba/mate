package org.mate.commons.interaction.action.espresso.actions;

import static android.view.KeyEvent.KEYCODE_SEARCH;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.os.Parcel;
import android.view.View;

import androidx.test.espresso.ViewAction;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a Search key Espresso action.
 */
public class SearchAction extends EspressoViewAction {

    public SearchAction() {
        super(EspressoViewActionType.SEARCH);
    }

    @Override
    public ViewAction getViewAction() {
        return pressKey(KEYCODE_SEARCH);
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        // This action can only be performed on the root view.
        return isRoot().matches(view);
    }

    @Override
    public String getCode() {
        return "pressKey(KEYCODE_SEARCH)";
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("android.view.KeyEvent.KEYCODE_SEARCH");
        imports.add("androidx.test.espresso.action.ViewActions.pressKey");
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

    protected SearchAction(Parcel in) {
        this();
    }

    public static final Creator<SearchAction> CREATOR = new Creator<SearchAction>() {
        @Override
        public SearchAction createFromParcel(Parcel source) {
            // We need to use the EspressoViewAction.CREATOR here, because we want to make sure
            // to remove the EspressoViewAction's type integer from the beginning of Parcel and call
            // the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (SearchAction) EspressoViewAction.CREATOR.createFromParcel(source);
        }

        @Override
        public SearchAction[] newArray(int size) {
            return new SearchAction[size];
        }
    };
}
