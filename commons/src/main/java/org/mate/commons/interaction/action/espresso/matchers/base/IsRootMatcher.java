package org.mate.commons.interaction.action.espresso.matchers.base;

import static androidx.test.espresso.matcher.ViewMatchers.isRoot;

import android.os.Parcel;
import android.view.View;

import org.hamcrest.Matcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcher;
import org.mate.commons.interaction.action.espresso.matchers.EspressoViewMatcherType;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements an Espresso Matcher for targeting the views that are at the root of the UI hierarchy.
 */
public class IsRootMatcher extends EspressoViewMatcher {

    public IsRootMatcher() {
        super(EspressoViewMatcherType.IS_ROOT);
    }

    @Override
    public String getCode() {
        return "isRoot()";
    }

    @Override
    public Matcher<View> getViewMatcher() {
        return isRoot();
    }

    @Override
    public Set<String> getNeededClassImports() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getNeededStaticImports() {
        HashSet<String> imports = new HashSet<>();
        imports.add("androidx.test.espresso.matcher.ViewMatchers.isRoot");
        return imports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public IsRootMatcher(Parcel in) {
        this();
    }

    public static final Creator<IsRootMatcher> CREATOR = new Creator<IsRootMatcher>() {
        @Override
        public IsRootMatcher createFromParcel(Parcel source) {
            // We need to use the EspressoViewMatcher.CREATOR here, because we want to make sure
            // to remove the EspressoViewMatcher's type integer from the beginning of Parcel and
            // call the appropriate constructor for this action.
            // Otherwise, the first integer will be read as data for an instance variable.
            return (IsRootMatcher) EspressoViewMatcher.CREATOR.createFromParcel(source);
        }

        @Override
        public IsRootMatcher[] newArray(int size) {
            return new IsRootMatcher[size];
        }
    };
}