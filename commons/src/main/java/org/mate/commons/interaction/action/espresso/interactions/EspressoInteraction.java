package org.mate.commons.interaction.action.espresso.interactions;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.test.espresso.Root;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.EspressoView;
import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;
import org.mate.commons.utils.AbstractCodeProducer;

/**
 * An abstract class representing an Espresso interaction on the Screen.
 */
public abstract class EspressoInteraction extends AbstractCodeProducer implements Parcelable {

    /**
     * The type of Espresso Interaction being represented by this instance.
     */
    protected final EspressoInteractionType type;

    /**
     * The view matcher used to identify the view to interact with.
     */
    protected final EspressoViewMatcher viewMatcher;

    /**
     * A root matcher to decide on which window the interaction should be performed.
     */
    protected Matcher<Root> rootMatcher = null;

    public EspressoInteraction(EspressoInteractionType type, EspressoViewMatcher viewMatcher) {
        this.type = type;
        this.viewMatcher = viewMatcher;
    }

    /**
     * @return the type of Espresso Interaction being represented by this instance.
     */
    public EspressoInteractionType getType() {
        return type;
    }

    /**
     * Set the root matcher for this interaction.
     * @param rootMatcher The root matcher to be set.
     * @return this instance
     */
    public EspressoInteraction inRoot(Matcher<Root> rootMatcher) {
        this.rootMatcher = rootMatcher;
        return this;
    }

    /**
     * Some interactions may need to setup internal parameters based
     * on the specific view they are interacting with
     * @param espressoView
     */
    public void setParametersForView(EspressoView espressoView) {
        // most interactions don't need to do anything
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected EspressoInteraction(Parcel in, EspressoInteractionType type) {
        this.type = type;
        this.viewMatcher = in.readParcelable(EspressoViewMatcher.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeParcelable(this.viewMatcher, flags);
    }

    /**
     * Auxiliary method to build an EspressoViewMatcher from a Parcel, using the correct subclass.
     * In order to do so, this method looks at the first integer in the Parcel.
     * Depending on the value, it will use the appropriate constructor from a subclass.
     *
     * DO NOT use here the CREATOR classes inside each of the EspressoViewMatcher subclasses.
     * Doing so will cause an infinite recursion, since they call this method in turn indirectly.
     *
     * @param source
     * @return
     */
    private static EspressoInteraction getConcreteClass(Parcel source) {
        int tmpType = source.readInt();
        EspressoInteractionType type = tmpType == -1 ? null :
                EspressoInteractionType.values()[tmpType];

        if (type == null) {
            throw new IllegalStateException("Found null value for EspressoInteraction type.");
        }

        switch (type) {
            case VIEW_INTERACTION:
                return new EspressoViewInteraction(source);
            case DATA_INTERACTION:
                return new EspressoDataInteraction(source);
            default:
                throw new IllegalStateException("Invalid int for EspressoInteraction type found: " +
                        type);
        }
    }

    public static final Creator<EspressoInteraction> CREATOR = new Creator<EspressoInteraction>() {
        @Override
        public EspressoInteraction createFromParcel(Parcel source) {
            return EspressoInteraction.getConcreteClass(source);
        }

        @Override
        public EspressoInteraction[] newArray(int size) {
            return new EspressoInteraction[size];
        }
    };

    public abstract ViewInteraction perform(ViewAction... actions);
}
