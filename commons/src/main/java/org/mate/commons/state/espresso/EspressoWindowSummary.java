package org.mate.commons.state.espresso;

import static org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcherType.WITH_WINDOW_TYPE;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.test.espresso.Root;

import org.mate.commons.interaction.action.espresso.EspressoView;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcher;
import org.mate.commons.interaction.action.espresso.root_matchers.EspressoRootMatcherType;
import org.mate.commons.interaction.action.espresso.root_matchers.IsDialogMatcher;
import org.mate.commons.interaction.action.espresso.root_matchers.IsPlatformPopupMatcher;
import org.mate.commons.interaction.action.espresso.root_matchers.WithWindowTypeMatcher;
import org.mate.commons.interaction.action.espresso.view_matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers_combination.RelativeMatcherCombination;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTreeNode;

import java.util.HashMap;
import java.util.Map;

public class EspressoWindowSummary implements Parcelable {

    /**
     * A list of discovered EspressoMatchers on this window.
     * The keys in the dictionary are the unique IDs of the views.
     */
    private Map<String, EspressoViewMatcher> viewMatchers = new HashMap<>();

    /**
     * A map of UI attributes for each view in this window.
     * The keys in the dictionary are the unique IDs of the views.
     */
    private Map<String, Map<String, String>> uiAttributes = new HashMap<>();

    /**
     * The type of window this is.
     */
    private int windowType;

    /**
     * The root matcher type for this window.
     */
    private EspressoRootMatcherType rootMatcherType;

    public EspressoWindowSummary(EspressoViewTree viewTree) {
        buildViewMatchers(viewTree);
        parseUiAttributes(viewTree);
        parseRootMatcherType(viewTree);

        windowType = viewTree.getWindowRoot().getWindowLayoutParams().get().type;
    }

    public int getWindowType() {
        return this.windowType;
    }

    public @Nullable EspressoViewMatcher getViewMatcher(String uniqueId) {
        return viewMatchers.get(uniqueId);
    }

    public @Nullable Map<String, String> getUIAttributes(String viewUniqueId) {
        return uiAttributes.get(viewUniqueId);
    }

    public Map<String, EspressoViewMatcher> getViewMatchers() {
        Map<String, EspressoViewMatcher> result = new HashMap<>();

        for (Map.Entry<String, EspressoViewMatcher> entry : viewMatchers.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private void buildViewMatchers(EspressoViewTree viewTree) {
        for (EspressoViewTreeNode node : viewTree.getAllNodes()) {
            if (node.getEspressoView().shouldBeSkipped()) {
                continue;
            }

            String uniqueId = node.getEspressoView().getUniqueId();

            RelativeMatcherCombination matcherCombination = RelativeMatcherCombination.
                    buildUnequivocalCombination(node, viewTree);

            if (matcherCombination == null) {
                // we weren't able to generate a unequivocal matcher combination for this view, skip
                // it.
                continue;
            }

            EspressoViewMatcher viewMatcher = matcherCombination.getEspressoViewMatcher();
            viewMatchers.put(uniqueId, viewMatcher);
        }
    }

    private void parseUiAttributes(EspressoViewTree viewTree) {
        for (EspressoViewTreeNode node : viewTree.getAllNodes()) {
            if (node.getEspressoView().shouldBeSkipped()) {
                continue;
            }

            String uniqueId = node.getEspressoView().getUniqueId();
            EspressoView espressoView = node.getEspressoView();

            Map<String, String> attributes = new HashMap<>(espressoView.getAllAttributes());

            uiAttributes.put(uniqueId, attributes);
        }
    }

    private void parseRootMatcherType(EspressoViewTree viewTree) {
        Root root = viewTree.getWindowRoot();

        IsDialogMatcher isDialogMatcher = new IsDialogMatcher();
        if (isDialogMatcher.getRootMatcher().matches(root)) {
            rootMatcherType = isDialogMatcher.getType();
            return;
        }

        IsPlatformPopupMatcher isPlatformPopupMatcher = new IsPlatformPopupMatcher();
        if (isPlatformPopupMatcher.getRootMatcher().matches(root)) {
            rootMatcherType = isPlatformPopupMatcher.getType();
            return;
        }

        rootMatcherType = WITH_WINDOW_TYPE;
    }

    public @Nullable
    EspressoRootMatcher getRootMatcher() {
        switch (rootMatcherType) {
            case IS_DIALOG:
                return new IsDialogMatcher();
            case IS_PLATFORM_POPUP:
                return new IsPlatformPopupMatcher();
            case WITH_WINDOW_TYPE:
                return new WithWindowTypeMatcher(windowType);
            default:
                throw new IllegalStateException("Invalid EspressoRootMatcher type found: " +
                        rootMatcherType);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.viewMatchers.size());
        for (Map.Entry<String, EspressoViewMatcher> entry : this.viewMatchers.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }

        dest.writeInt(this.uiAttributes.size());
        for (Map.Entry<String, Map<String, String>> entry : this.uiAttributes.entrySet()) {
            dest.writeString(entry.getKey());

            Map<String, String> attrs = entry.getValue();
            dest.writeInt(attrs.size());
            for (Map.Entry<String, String> auxEntry : attrs.entrySet()) {
                dest.writeString(auxEntry.getKey());
                dest.writeString(auxEntry.getValue());
            }
        }

        dest.writeInt(this.windowType);

        dest.writeInt(this.rootMatcherType == null ? -1 : this.rootMatcherType.ordinal());
    }

    protected EspressoWindowSummary(Parcel in) {
        int viewMatchersSize = in.readInt();
        this.viewMatchers = new HashMap<>(viewMatchersSize);
        for (int i = 0; i < viewMatchersSize; i++) {
            String key = in.readString();
            EspressoViewMatcher value = in.readParcelable(EspressoViewMatcher.class.getClassLoader());
            this.viewMatchers.put(key, value);
        }

        int uiAttributesSize = in.readInt();
        this.uiAttributes = new HashMap<>(uiAttributesSize);
        for (int i = 0; i < uiAttributesSize; i++) {
            String key = in.readString();

            int attrsSize = in.readInt();
            Map<String, String> attrs = new HashMap<>(attrsSize);
            for (int j = 0; j < attrsSize; j++) {
                String attrKey = in.readString();
                String attrValue = in.readString();
                attrs.put(attrKey, attrValue);
            }

            this.uiAttributes.put(key, attrs);
        }

        this.windowType = in.readInt();

        int tmpRootType = in.readInt();
        this.rootMatcherType = tmpRootType == -1 ? null :
                EspressoRootMatcherType.values()[tmpRootType];
    }

    public static final Creator<EspressoWindowSummary> CREATOR = new Creator<EspressoWindowSummary>() {
        @Override
        public EspressoWindowSummary createFromParcel(Parcel source) {
            return new EspressoWindowSummary(source);
        }

        @Override
        public EspressoWindowSummary[] newArray(int size) {
            return new EspressoWindowSummary[size];
        }
    };
}
