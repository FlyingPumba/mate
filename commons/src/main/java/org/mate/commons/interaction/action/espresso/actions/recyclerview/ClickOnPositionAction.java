package org.mate.commons.interaction.action.espresso.actions.recyclerview;

import static androidx.test.espresso.action.ViewActions.click;

import android.os.Parcel;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;

import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewActionType;
import org.mate.commons.utils.MATELog;

import java.util.HashSet;
import java.util.Set;

public class ClickOnPositionAction extends EspressoViewAction {

    private int index;

    public ClickOnPositionAction() {
        super(EspressoViewActionType.RECYCLER_CLICK_ON_POSITION);
        index = 1;
    }

    @Override
    public ViewAction getViewAction() {
        return RecyclerViewActions.actionOnItemAtPosition(this.index, click());
    }

    @Override
    public String getCode() {
        String str = String.format("RecyclerViewActions.actionOnItemAtPosition(%d, click())", index);
        return str;
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        boolean isValid = false;
        if (view instanceof RecyclerView) {
            RecyclerView rv = (RecyclerView) view;
            int max = rv.getAdapter().getItemCount();
            if (max > 0) {
                this.setIndex((int) ((Math.random() * (max - 1)) + 1));
                isValid = true;

            }
        }
        return isValid;
    }

    @Override
    public Set<String> getNeededClassImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.contrib.RecyclerViewActions");
        return imports;
    }

    @Override
    public Set<String> getNeededStaticImports() {
        return new HashSet<>();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.index);
    }

    public ClickOnPositionAction(Parcel in) {
        this();
        this.setIndex(in.readInt());
    }

    private void setIndex(int newIndex) {
        this.index = newIndex;
    }

}
