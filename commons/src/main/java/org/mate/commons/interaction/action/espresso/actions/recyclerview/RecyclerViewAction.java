package org.mate.commons.interaction.action.espresso.actions.recyclerview;

import android.os.Parcel;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.mate.commons.interaction.action.espresso.EspressoView;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewAction;
import org.mate.commons.interaction.action.espresso.actions.EspressoViewActionType;
import org.mate.commons.utils.Randomness;

/**
 * Base class for all Espresso actions that operate on a specific item position of a RecyclerView.
 */
public abstract class RecyclerViewAction extends EspressoViewAction {

    protected int index = -1;

    RecyclerViewAction(EspressoViewActionType type) {
        super(type);
    }

    @Override
    public boolean isValidForEnabledView(View view) {
        boolean isRecyclerView = view instanceof RecyclerView;
        if (!isRecyclerView) {
            return false;
        }

        RecyclerView rv = (RecyclerView) view;

        return rv.getAdapter() != null;
    }

    @Override
    public void setParametersForView(EspressoView view) {
        RecyclerView rv = (RecyclerView) view.getView();
        int itemCount = rv.getAdapter().getItemCount();
        this.index = Randomness.getRnd().nextInt(itemCount + 1);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.index);
    }
}
