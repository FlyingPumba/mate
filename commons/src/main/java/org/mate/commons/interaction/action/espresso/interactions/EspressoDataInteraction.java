package org.mate.commons.interaction.action.espresso.interactions;

import static androidx.test.espresso.Espresso.onData;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.widget.AdapterView;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;

import org.mate.commons.interaction.action.espresso.EspressoView;
import org.mate.commons.interaction.action.espresso.data_matchers.EspressoDataMatcher;
import org.mate.commons.interaction.action.espresso.data_matchers.recursive.AllOfDataMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;
import org.mate.commons.utils.Randomness;

import java.util.HashSet;
import java.util.Set;

/**
 * A wrapper around the Espresso DataInteraction class.
 * This class is used to represent the interactions that can be performed on an AdapterView.
 *
 * The Espresso DataInteraction class provides an interface to interact with data displayed in
 * AdapterViews. This interface builds on top of ViewInteraction and should be the preferred way
 * to interact with elements displayed inside AdapterViews.
 *
 * This is necessary because an AdapterView may not load all the data held by its Adapter into
 * the view hierarchy until a user interaction makes it necessary. Also it is more fluent / less
 * brittle to match upon the data object being rendered into the display then the rendering itself.
 *
 * The check and perform method operate on the top level child of the adapter view, if you need
 * to operate on a subview (eg: a Button within the list) use the onChildView method before
 * calling perform or check.
 */
public class EspressoDataInteraction extends EspressoInteraction {

    /**
     * The data matcher used to identify the data inside the AdapterView to interact with.
     */
    private final EspressoDataMatcher dataMatcher;

    /**
     * The position of the item to perform the interaction on.
     */
    private int position = -1;

    public EspressoDataInteraction(EspressoViewMatcher adapterViewMatcher) {
        this(adapterViewMatcher, new AllOfDataMatcher());
    }

    public EspressoDataInteraction(EspressoViewMatcher adapterViewMatcher, EspressoDataMatcher dataMatcher) {
        super(EspressoInteractionType.DATA_INTERACTION, adapterViewMatcher);
        this.dataMatcher = dataMatcher;
    }

    public DataInteraction getDataInteraction() {
        return onData(dataMatcher.getDataMatcher())
                .inAdapterView(viewMatcher.getViewMatcher())
                .atPosition(position);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getCode() {
        return String.format("onData(%s).inAdapterView(%s).atPosition(%d)",
                dataMatcher.getCode(),
                viewMatcher.getCode(),
                position);
    }

    @Override
    public ViewInteraction perform(ViewAction... actions) {
        DataInteraction dataInteraction = getDataInteraction();

        if (rootMatcher != null) {
            dataInteraction.inRoot(rootMatcher);
        }

        return dataInteraction.perform(actions);
    }

    @Override
    public void setParametersForView(EspressoView espressoView) {
        AdapterView adapter = (AdapterView) espressoView.getView();
        int itemCount = adapter.getCount();
        this.position = Randomness.getRnd().nextInt(itemCount);
    }

    @Override
    public Set<String> getNeededClassImports() {
        HashSet<String> imports = new HashSet<>();

        imports.addAll(viewMatcher.getNeededClassImports());
        imports.addAll(dataMatcher.getNeededClassImports());

        return imports;
    }

    @Override
    public Set<String> getNeededStaticImports() {
        HashSet<String> imports = new HashSet<>();

        imports.add("androidx.test.espresso.Espresso.onData");
        imports.addAll(viewMatcher.getNeededStaticImports());
        imports.addAll(dataMatcher.getNeededStaticImports());

        return imports;
    }

    protected EspressoDataInteraction(Parcel in) {
        super(in, EspressoInteractionType.DATA_INTERACTION);
        position = in.readInt();
        dataMatcher = in.readParcelable(EspressoDataMatcher.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.position);
        dest.writeParcelable(this.dataMatcher, flags);
    }

    public static final Creator<EspressoViewInteraction> CREATOR = new Creator<EspressoViewInteraction>() {
        @Override
        public EspressoViewInteraction createFromParcel(Parcel source) {
            // We need to use the EspressoViewInteraction.CREATOR here, because we want to make sure
            // to remove the EspressoViewInteraction's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (EspressoViewInteraction) EspressoInteraction.CREATOR.createFromParcel(source);
        }

        @Override
        public EspressoViewInteraction[] newArray(int size) {
            return new EspressoViewInteraction[size];
        }
    };

}
