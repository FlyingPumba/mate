package org.mate.commons.interaction.action.espresso.actions.recyclerview;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;

import org.mate.commons.interaction.action.espresso.actions.EspressoViewActionType;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements an Espresso action to click on a certain position in a RecyclerView.
 */
public class ClickOnPositionAction extends RecyclerViewAction {

    public ClickOnPositionAction() {
        super(EspressoViewActionType.RECYCLER_CLICK_ON_POSITION);
    }

    public ClickOnPositionAction(int position) {
        super(EspressoViewActionType.RECYCLER_CLICK_ON_POSITION);
        this.position = position;
    }

    @Override
    public ViewAction getViewAction() {
        return actionOnItemAtPosition(this.position, click());
    }

    @Override
    public String getCode() {
        return String.format("actionOnItemAtPosition(%d, click())", position);
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.action.ViewActions.click");
        imports.add("androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition");
        return imports;
    }

    public ClickOnPositionAction(Parcel in) {
        this(in.readInt());
    }
}
