package org.mate.commons.interaction.action.espresso.actions.recyclerview;

import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;

import android.os.Parcel;

import androidx.test.espresso.ViewAction;

import org.mate.commons.interaction.action.espresso.actions.EspressoViewActionType;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements an Espresso action to scroll to a certain position in a RecyclerView.
 */
public class ScrollToPositionAction extends RecyclerViewAction {

    public ScrollToPositionAction() {
        super(EspressoViewActionType.RECYCLER_SCROLL_TO_POSITION);
    }

    public ScrollToPositionAction(int index) {
        super(EspressoViewActionType.RECYCLER_SCROLL_TO_POSITION);
        this.index = index;
    }

    @Override
    public ViewAction getViewAction() {
        return scrollToPosition(index);
    }

    @Override
    public String getCode() {
        return String.format("scrollToPosition(%d)", index);
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        Set<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition");
        return imports;
    }

    public ScrollToPositionAction(Parcel in) {
        this(in.readInt());
    }
}
