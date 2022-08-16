package org.mate.commons.interaction.action.espresso.actions.recyclerview;

import android.os.Parcel;
import android.os.RemoteException;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;

import org.mate.commons.exceptions.AUTCrashException;
import org.mate.commons.interaction.action.espresso.EspressoView;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewActionType;
import org.mate.commons.utils.MATELog;

import java.util.HashSet;
import java.util.Set;

public class ScrollToPositionAction extends EspressoViewAction {


    private int index;

    public ScrollToPositionAction(int anIndex) {
        super(EspressoViewActionType.RECYCLER_SCROLL_TO_POSITION);
        index = anIndex;
    }


    @Override
    public ViewAction getViewAction() {
        return RecyclerViewActions.scrollToPosition(index);
    }

    @Override
    public String getCode() {
        String str = String.format("RecyclerViewActions.scrollToPosition(%d)", index);

        return str;
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        return view instanceof RecyclerView;
    }

    @Override
    public void setParametersForView(EspressoView view) {
        RecyclerView rv = (RecyclerView) view.getView();
        int max = rv.getAdapter().getItemCount();
        if (max > 0) {
            this.setIndex((int) ((Math.random() * (max - 1)) + 1));
        }
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

    public ScrollToPositionAction(Parcel in) {
        this(in.readInt());
    }

    private void setIndex(int newIndex) {
        this.index = newIndex;
    }

}
