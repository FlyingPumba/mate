package org.mate.commons.state.espresso;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import org.mate.commons.interaction.action.espresso.interactions.EspressoInteraction;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcherType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EspressoScreenSummary implements Parcelable {

    private List<EspressoWindowSummary> espressoWindowSummaries = new ArrayList<>();

    public EspressoScreenSummary(EspressoScreen espressoScreen) {
        for (EspressoWindow espressoWindow : espressoScreen.getWindows()) {
            espressoWindowSummaries.add(espressoWindow.getSummary());
        }
    }

    public int getTopWindowType() {
        return this.espressoWindowSummaries.get(0).getWindowType();
    }

    public Map<String, EspressoInteraction> getInteractionsInScreen() {
        Map<String, EspressoInteraction> result = new HashMap<>();

        for (EspressoWindowSummary espressoWindow : espressoWindowSummaries) {
            Map<String, EspressoInteraction> viewInteractions = espressoWindow.getInteractions();
            result.putAll(viewInteractions);
        }

        return result;
    }

    public Map<String, EspressoInteraction> getInteractionsInTopWindow() {
        return espressoWindowSummaries.get(0).getInteractions();
    }

    /**
     * Get Interactions from an old summary that not longer exist in this summary.
     * @param oldSummary The old summary.
     * @return The Interactions that are not in this summary.
     */
    public Map<String, EspressoInteraction> getDisappearingInteractions(EspressoScreenSummary oldSummary) {
        Map<String, EspressoInteraction> interactions = new HashMap<>();

        Map<String, EspressoInteraction> oldInteractions = oldSummary.getInteractionsInScreen();
        Collection<EspressoInteraction> newInteractions = getInteractionsInScreen().values();

        for (Map.Entry<String, EspressoInteraction> entry : oldInteractions.entrySet()) {
            EspressoInteraction oldMatcher = entry.getValue();

            if (!containsInteraction(newInteractions, oldMatcher)) {
                interactions.put(entry.getKey(), entry.getValue());
            }
        }

        return interactions;
    }

    /**
     * Get Interactions from this summary that are not in an old summary.
     * @param oldSummary The old summary.
     * @return The Interactions that are not in the old summary.
     */
    public Map<String, EspressoInteraction> getAppearingInteractions(EspressoScreenSummary oldSummary) {
        Map<String, EspressoInteraction> interactions = new HashMap<>();

        Collection<EspressoInteraction> oldInteractions =
                oldSummary.getInteractionsInScreen().values();
        Map<String, EspressoInteraction> newInteractions = getInteractionsInScreen();

        for (Map.Entry<String, EspressoInteraction> entry : newInteractions.entrySet()) {
            EspressoInteraction newMatcher = entry.getValue();

            if (!containsInteraction(oldInteractions, newMatcher)) {
                interactions.put(entry.getKey(), entry.getValue());
            }
        }

        return interactions;
    }

    /**
     * Get Interactions from the top window of this summary that are also in the top window of an
     * old summary.
     * @param oldSummary The old summary.
     * @return The Interactions that are also in the old summary.
     */
    public Map<String, EspressoInteraction> getCommonInteractionsInTopWindow(EspressoScreenSummary oldSummary) {
        Map<String, EspressoInteraction> interactions = new HashMap<>();

        Collection<EspressoInteraction> oldInteractions =
                oldSummary.getInteractionsInTopWindow().values();
        Map<String, EspressoInteraction> newInteractions = getInteractionsInTopWindow();

        for (Map.Entry<String, EspressoInteraction> entry : newInteractions.entrySet()) {
            EspressoInteraction newMatcher = entry.getValue();

            if (containsInteraction(oldInteractions, newMatcher)) {
                interactions.put(entry.getKey(), entry.getValue());
            }
        }

        return interactions;
    }

    public Map<String, String> getUiAttributes(String viewUniqueId) {
        Map<String, String> uiAttributes = new HashMap<>();

        for (EspressoWindowSummary espressoWindow : espressoWindowSummaries) {
            uiAttributes = espressoWindow.getUIAttributes(viewUniqueId);

            if (uiAttributes != null) {
                break;
            }
        }

        return uiAttributes;
    }

    /**
     * Returns true if a interaction with the same code is present in a collection.
     * @param interactions The collection of interactions.
     * @param interaction The interaction to check.
     * @return True if the interaction is present in the collection.
     */
    private boolean containsInteraction(Collection<EspressoInteraction> interactions,
                                        EspressoInteraction interaction) {
        String interactionCode = interaction.getCode();

        for (EspressoInteraction m : interactions) {
            if (m.getCode().equals(interactionCode)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(this.espressoWindowSummaries);
    }

    protected EspressoScreenSummary(Parcel in) {
        this.espressoWindowSummaries = new ArrayList<>();
        in.readList(this.espressoWindowSummaries, EspressoWindowSummary.class.getClassLoader());
    }

    public static final Creator<EspressoScreenSummary> CREATOR = new Creator<EspressoScreenSummary>() {
        @Override
        public EspressoScreenSummary createFromParcel(Parcel source) {
            return new EspressoScreenSummary(source);
        }

        @Override
        public EspressoScreenSummary[] newArray(int size) {
            return new EspressoScreenSummary[size];
        }
    };

    /**
     * Returns the root matcher for the window of the given view
     * @param viewUniqueId The view unique id
     * @return A root matcher or null.
     */
    public @Nullable
    EspressoRootMatcher getRootMatcher(String viewUniqueId) {
        EspressoRootMatcher rootMatcher = null;

        for (int i = 0, espressoWindowSummariesSize = espressoWindowSummaries.size(); i < espressoWindowSummariesSize; i++) {
            EspressoWindowSummary espressoWindow = espressoWindowSummaries.get(i);
            EspressoInteraction interaction = espressoWindow.getInteraction(viewUniqueId);

            if (interaction != null) {
                rootMatcher = espressoWindow.getRootMatcher();

                if (i == 0) {
                    // The view was found in the top-most window.
                    // When this happens, we only need to use a root matcher if the window is
                    // a popup.
                    if (rootMatcher != null && rootMatcher.getType() != EspressoRootMatcherType.IS_PLATFORM_POPUP) {
                        rootMatcher = null;
                    }

                    break;
                }

                break;
            }
        }

        return rootMatcher;
    }
}
