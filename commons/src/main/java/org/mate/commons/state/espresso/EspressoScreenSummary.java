package org.mate.commons.state.espresso;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcherType;
import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;

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

    public Map<String, EspressoViewMatcher> getMatchersInScreen(boolean includeAndroidViews) {
        Map<String, EspressoViewMatcher> result = new HashMap<>();

        for (EspressoWindowSummary espressoWindow : espressoWindowSummaries) {
            Map<String, EspressoViewMatcher> viewMatchers = espressoWindow.getViewMatchers(includeAndroidViews);
            result.putAll(viewMatchers);
        }

        return result;
    }

    /**
     * Get ViewMatchers from an old summary that not longer exist in this summary.
     * @param oldSummary The old summary.
     * @return The ViewMatchers that are not in this summary.
     */
    public Map<String, EspressoViewMatcher> getDisappearingViewMatchers(EspressoScreenSummary oldSummary) {
        Map<String, EspressoViewMatcher> matchers = new HashMap<>();

        Map<String, EspressoViewMatcher> oldMatchers = oldSummary.getMatchersInScreen(false);
        Collection<EspressoViewMatcher> newMatchers = getMatchersInScreen(false).values();

        for (Map.Entry<String, EspressoViewMatcher> entry : oldMatchers.entrySet()) {
            EspressoViewMatcher oldMatcher = entry.getValue();

            if (!containsMatcher(newMatchers, oldMatcher)) {
                matchers.put(entry.getKey(), entry.getValue());
            }
        }

        return matchers;
    }

    /**
     * Get ViewMatchers from this summary that are not in an old summary.
     * @param oldSummary The old summary.
     * @return The ViewMatchers that are not in the old summary.
     */
    public Map<String, EspressoViewMatcher> getAppearingViewMatchers(EspressoScreenSummary oldSummary) {
        Map<String, EspressoViewMatcher> matchers = new HashMap<>();

        Collection<EspressoViewMatcher> oldMatchers = oldSummary.getMatchersInScreen(false).values();
        Map<String, EspressoViewMatcher> newMatchers = getMatchersInScreen(false);

        for (Map.Entry<String, EspressoViewMatcher> entry : newMatchers.entrySet()) {
            EspressoViewMatcher newMatcher = entry.getValue();

            if (!containsMatcher(oldMatchers, newMatcher)) {
                matchers.put(entry.getKey(), entry.getValue());
            }
        }

        return matchers;
    }

    /**
     * Get ViewMatchers from this summary that are also in an old summary.
     * @param oldSummary The old summary.
     * @return The ViewMatchers that are also in the old summary.
     */
    public Map<String, EspressoViewMatcher> getCommonViewMatchers(EspressoScreenSummary oldSummary) {
        Map<String, EspressoViewMatcher> matchers = new HashMap<>();

        Collection<EspressoViewMatcher> oldMatchers = oldSummary.getMatchersInScreen(false).values();
        Map<String, EspressoViewMatcher> newMatchers = getMatchersInScreen(false);

        for (Map.Entry<String, EspressoViewMatcher> entry : newMatchers.entrySet()) {
            EspressoViewMatcher newMatcher = entry.getValue();

            if (containsMatcher(oldMatchers, newMatcher)) {
                matchers.put(entry.getKey(), entry.getValue());
            }
        }

        return matchers;
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
     * Returns true if a matcher with the same code is present in a collection.
     * @param matchers The collection of matchers.
     * @param matcher The matcher to check.
     * @return True if the matcher is present in the collection.
     */
    private boolean containsMatcher(Collection<EspressoViewMatcher> matchers,
                                    EspressoViewMatcher matcher) {
        String matcherCode = matcher.getCode();

        for (EspressoViewMatcher m : matchers) {
            if (m.getCode().equals(matcherCode)) {
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
    public @Nullable EspressoRootMatcher getRootMatcher(String viewUniqueId) {
        EspressoRootMatcher rootMatcher = null;

        for (int i = 0, espressoWindowSummariesSize = espressoWindowSummaries.size(); i < espressoWindowSummariesSize; i++) {
            EspressoWindowSummary espressoWindow = espressoWindowSummaries.get(i);
            EspressoViewMatcher viewMatcher = espressoWindow.getViewMatcher(viewUniqueId);

            if (viewMatcher != null) {
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
