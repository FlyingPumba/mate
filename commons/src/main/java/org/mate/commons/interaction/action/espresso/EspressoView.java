package org.mate.commons.interaction.action.espresso;

import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.ViewMatchers;

import org.mate.commons.interaction.action.espresso.layout_inspector.common.Resource;
import org.mate.commons.interaction.action.espresso.layout_inspector.property.LayoutParamsTypeTree;
import org.mate.commons.interaction.action.espresso.layout_inspector.property.Property;
import org.mate.commons.interaction.action.espresso.layout_inspector.property.ViewNode;
import org.mate.commons.interaction.action.espresso.layout_inspector.property.ViewTypeTree;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;
import org.mate.commons.utils.MATELog;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Wrapper around the View class.
 * It provides useful information for building Espresso ViewMatchers and ViewActions.
 */
public class EspressoView {

    /**
     * Android views that should be skipped if they have any of these resource names.
     */
    private static final String[] ANDROID_VIEW_RESOURCE_NAMES_TO_SKIP = {
        // The view that marks the cursor for editing an EditText. It will appear and disappear
        // constantly over an EditText, causing flakiness in our tests
        "insertion_handle",
        // Same as above, but for the selection handles.
        "selection_end_handle",
        "selection_start_handle",
        // Any item in a contextual menu (e.g., "Paste", "Copy", "Autofill", etc.). They all
        // cause flakiness.
        "floating_toolbar_menu_item_text",
        // The "three dots" button on a contextual menu.
        "overflow"
    };

    /**
     * An ad-hoc ID that is unique for this View in the current activity.
     */
    private String uniqueId;

    /**
     * The View instance that we are wrapping.
     */
    private final View view;

    /**
     * The Activity name in which this View was found.
     */
    private final String activityName;

    public EspressoView(View view, String activityName) {
        this.view = view;
        this.activityName = activityName;
    }

    /**
     * Generate a unique ID for the wrapped View.
     * @param viewTree the EspressoViewTree that contains this EspressoView.
     */
    public void generateUniqueId(EspressoViewTree viewTree) {
        if (viewTree.getNodesById(getId()).size() > 1) {
            // If there are multiple views with the same ID, we need to find another unique ID.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // On API 29+ we can use the View.getUniqueDrawingId() method to obtain a unique
                // ID that is used by the drawing system.
                uniqueId = String.valueOf(view.getUniqueDrawingId());
            } else {
                uniqueId = UUID.randomUUID().toString();
            }

        } else {
            // Otherwise, we can use the ID of the View.
            uniqueId = getId().toString();
        }

        // Add activity name as prefix of unique id, so we can identify the activity in which the
        // view is found.
        uniqueId = activityName + "." + uniqueId;
    }

    /**
     * @return An ad-hoc ID that is unique for this View in the current activity.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * @return the Activity name in which the View was found.
     */
    public String getActivity() {
        return activityName;
    }

    /**
     * @return The View instance that we are wrapping.
     */
    public View getView() {
        return view;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EspressoView that = (EspressoView) o;
        return uniqueId.equals(that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }

    /**
     * @return the View's ID (a.k.a., resource ID) if it has one, -1 otherwise.
     */
    public Integer getId() {
        return view.getId();
    }

    /**
     * @return the View's class name.
     */
    public String getClassName() {
        return view.getClass().getName();
    }

    /**
     * @return the View's content description if it has one, null otherwise.
     */
    public @Nullable
    String getContentDescription() {
        CharSequence contentDescription = view.getContentDescription();
        if (contentDescription != null) {
            return contentDescription.toString();
        }

        return null;
    }

    /**
     * @return the View's text if it has one, null otherwise.
     */
    public @Nullable String getText() {
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();
            if (text != null) {
                return text.toString();
            }
        }

        return null;
    }

    /**
     * @return the View's hint if it has one, null otherwise.
     */
    public @Nullable
    String getHint() {
        if (view instanceof TextView) {
            CharSequence hint = ((TextView) view).getHint();
            if (hint != null) {
                return hint.toString();
            }
        }

        return null;
    }

    /**
     * @return the View's Error text if it has one, null otherwise.
     */
    public @Nullable
    String getErrorText() {
        if (view instanceof EditText) {
            CharSequence error = ((EditText) view).getError();
            if (error != null) {
                return error.toString();
            }
        }

        return null;
    }

    /**
     * @return whether this View is checked or not. Returns null if the View is not a Checkable.
     */
    public @Nullable Boolean isChecked() {
        if (view instanceof Checkable) {
            return ((Checkable) view).isChecked();
        }

        return null;
    }

    /**
     * @return whether this View has links or not. Returns null if the View is not a TextView.
     */
    public @Nullable Boolean hasLinks() {
        if (view instanceof TextView) {
            return ((TextView) view).getUrls().length > 0;
        }

        return null;
    }

    /**
     * @return the number of children this View has. Returns null if the View is not a ViewGroup.
     */
    public @Nullable Integer getChildCount() {
        if (view instanceof ViewGroup) {
            return ((ViewGroup) view).getChildCount();
        }

        return null;
    }

    /**
     * @return the input type of the View. Returns null if the View is not a EditText.
     */
    public @Nullable Integer getInputType() {
        if (view instanceof EditText) {
            return ((EditText) view).getInputType();
        }

        return null;
    }

    /**
     * @return the number of index of this view in its parent's children. Returns null if the
     * View does not have a parent or if the parent is not a ViewGroup.
     */
    public @Nullable Integer getParentIndex() {
        ViewParent parent = view.getParent();

        if (parent == null) {
            return null;
        }

        if (!(parent instanceof ViewGroup)) {
            return null;
        }

        ViewGroup parentGroup = (ViewGroup) parent;

        int indexOfChild = parentGroup.indexOfChild(view);

        return indexOfChild != -1 ? indexOfChild : null;
    }

    /**
     * This method's implementation was taken from of Espresso's WithResourceNameMatcher class.
     * @return the View's resource name if it has one, null otherwise.
     */
    public @Nullable String getResourceEntryName() {
        int id = view.getId();

        if (id == View.NO_ID) {
            // view.getId() was View.NO_ID
            return null;
        }

        if (view.getResources() == null) {
            // view.getResources() was null, can't resolve resource name
            return null;
        }
        if (isViewIdGenerated(id)) {
            // view.getId() was generated by a call to View.generateViewId()
            return null;
        }

        String resourceName = safeGetResourceEntryName(view.getResources(), id);

        if (resourceName == null) {
            MATELog.log_warn(String.format("Unable to find resource entry name for view with id %d",
                    id));
            return null;
        }

        return resourceName;
    }

    /**
     * Returns the full resource name for the wrapped View's ID.
     * This name is a single string of the form "package:type/entry".
     * @return A string holding the name of the resource.
     */
    public @Nullable String getFullResourceName() {
        int id = view.getId();

        if (id == View.NO_ID) {
            // view.getId() was View.NO_ID
            return null;
        }

        if (view.getResources() == null) {
            // view.getResources() was null, can't resolve resource name
            return null;
        }
        if (isViewIdGenerated(id)) {
            // view.getId() was generated by a call to View.generateViewId()
            return null;
        }

        String resourceName = safeGetFullResourceName(view.getResources(), id);

        if (resourceName == null) {
            MATELog.log_warn(String.format("Unable to find full resource name for view with id %d",
                    id));
            return null;
        }

        return resourceName;
    }

    /**
     * @return a boolean indicating whether the wrapped view is an Android view (e.g., created by
     * the OS) or not.
     */
    public boolean isAndroidView() {
        String resourceName = getFullResourceName();
        if (resourceName == null) {
            return false;
        }

        return resourceName.startsWith("android")
                || resourceName.startsWith("com.google.android")
                || resourceName.startsWith("com.android");
    }

    /**
     * Returns a boolean indicating whether the wrapped view should be skipped when analyzing
     * which actions or matchers are there in the screen.
     *
     * In particular, we skip Android views with resource names mentioned in
     * ANDROID_VIEW_RESOURCE_NAMES_TO_SKIP. Those are Android views that tend to be problematic,
     * so we skip them as well.
     * We do not skip all Android views because there are some auto-generated
     * Android Views for which we want to generate actions. For example, the items/entries inside
     * a Spinner widget have an id "android:id/text1" and class "AppCompatTextView" (and thus
     * have text), and we can clearly build a unequivocal matcher for them.
     *
     * We also skip most of the View Groups that do not have text. E.g., an intermediate
     * LinearLayout in the UI Hierarchy.
     * An exception to this rule is a RecyclerView, which is a ViewGroup but we will generate
     * specific actions for it.
     *
     * @return a boolean
     */
    public boolean shouldBeSkipped() {
        boolean isAndroidView = isAndroidView();
        String resourceName = getResourceEntryName();

        if (isAndroidView &&
                Arrays.asList(ANDROID_VIEW_RESOURCE_NAMES_TO_SKIP).contains(resourceName)) {
            return true;
        }

        String viewText = getText();
        boolean noText = viewText == null || viewText.isEmpty();

        boolean isViewGroup = view instanceof ViewGroup;
        boolean isRecyclerView = view instanceof RecyclerView;

        return isViewGroup && !isRecyclerView && noText;
    }

    /**
     * This method's implementation was taken from Espresso's ViewMatchers#safeGetResourceEntryName
     * method.
     *
     * Get the resource entry name given an integer identifier in a safe manner. This means:
     *
     * <ul>
     *   <li>Handling {@link Resources.NotFoundException} if thrown.
     *   <li>Not querying the resources if the identifier is generated. This would otherwise always
     *       fail and will log an error. This should be avoided because in some testing frameworks,
     *       logging an error will make the test fail.
     * </ul>
     *
     * @param res The {@link Resources} to query for the ID.
     * @param id The ID to query.
     * @return The resource entry name or {@code null} if not found.
     * @see #isViewIdGenerated(int)
     * @see Resources#getResourceEntryName(int)
     */
    private static String safeGetResourceEntryName(Resources res, int id) {
        try {
            return isViewIdGenerated(id) ? null : res.getResourceEntryName(id);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    /**
     * Same as {@link #safeGetResourceEntryName(Resources, int)} but returns the full resource name.
     * @param res The {@link Resources} to query for the ID.
     * @param id The ID to query.
     * @return The resource full name or {@code null} if not found.
     */
    private static String safeGetFullResourceName(Resources res, int id) {
        try {
            return isViewIdGenerated(id) ? null : res.getResourceName(id);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    /**
     * IDs generated by {@link View#generateViewId} will fail if used as a resource ID in attempted
     * resources lookups. This now logs an error in API 28, causing test failures. This method is
     * taken from {@link View#isViewIdGenerated} to prevent resource lookup to check if a view id was
     * generated.
     */
    private static boolean isViewIdGenerated(int id) {
        return (id & 0xFF000000) == 0 && (id & 0x00FFFFFF) != 0;
    }

    /**
     * Returns the basic attributes of the wrapped view that are obtained through its getters.
     * @return a map of attributes.
     */
    public Map<String, String> getBasicViewAttributes() {
        Map<String, String> attributes = new HashMap<>();

        attributes.put("x", String.valueOf(view.getX()));
        attributes.put("y", String.valueOf(view.getY()));

        attributes.put("width", String.valueOf(view.getWidth()));
        attributes.put("height", String.valueOf(view.getHeight()));

        attributes.put("text", getText());
        attributes.put("contentDescription", getContentDescription());
        attributes.put("hint", getHint());
        attributes.put("errorText", getErrorText());

        attributes.put("enabled", view.isEnabled() ? "true" : "false");
        attributes.put("focused", view.isFocused() ? "true" : "false");
        attributes.put("hasFocus", view.hasFocus() ? "true" : "false");
        attributes.put("selected", view.isSelected() ? "true" : "false");
        attributes.put("clickable", view.isClickable() ? "true" : "false");
        attributes.put("focusable", view.isFocusable() ? "true" : "false");

        Boolean hasLinks = hasLinks();
        attributes.put("hasLinks", hasLinks != null ? hasLinks.toString() : null);

        Boolean checked = isChecked();
        attributes.put("checked", checked != null ? checked.toString() : null);

        Integer childCount = getChildCount();
        attributes.put("childCount", childCount != null ? childCount.toString() : null);

        Integer inputType = getInputType();
        attributes.put("inputType", inputType != null ? inputType.toString() : null);

        Integer parentIndex = getParentIndex();
        attributes.put("parentIndex", parentIndex != null ? parentIndex.toString() : null);

        attributes.put("alpha", String.valueOf(view.getAlpha()));

        // Add a special "is_displayed" UI attribute.
        // This can only be computed with certainty if we have the view and can access the
        // "getGlobalVisibleRect" method.
        // Attempts to determine if the view is displayed or not using the width and height
        // properties may not work when the view has special values such as "wrap_content".
        boolean isDisplayed =
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE).matches(view) &&
                        view.getGlobalVisibleRect(new Rect());
        if (isDisplayed) {
            attributes.put("is_displayed", "true");
        } else {
            attributes.put("is_displayed", "false");
        }

        return attributes;
    }

    /**
     * Returns the attributes of the wrapped view that are found in the instance itself.
     * @return a map of attributes.
     */
    public Map<String, String> getInternalViewAttributes() {
        Map<String, String> attributes = new HashMap<>();

        // Get the "mAttributes" field of the wrapped view using reflection
        // If it fails, we just return an empty map.
        try {
            @SuppressLint("SoonBlockedPrivateApi")
            Field mAttributesField = View.class.getDeclaredField("mAttributes");

            mAttributesField.setAccessible(true);
            String[] mAttributesFieldValue = (String[]) mAttributesField.get(view);

            if (mAttributesFieldValue != null) {
                // Turn array of attributes into a map.
                for (int i = 0; i < mAttributesFieldValue.length; i += 2) {
                    attributes.put(mAttributesFieldValue[i], mAttributesFieldValue[i + 1]);
                }
            }
        } catch (Exception e) {
            MATELog.log_error("Unable to get mAttributes field of View");
        }

        return attributes;
    }

    /**
     * Returns a very extensive list of attributes for the wrapped view.
     * This code is inspired by the code in the Android Studio's Layout Inspector.
     * However, it requires a minimum API level of 29.
     * @return a map of attributes.
     */
    public Map<String, String> getLayoutInspectorAttributes() {
        Map<String, String> attributes = new HashMap<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ViewTypeTree typeTree = new ViewTypeTree();
            LayoutParamsTypeTree layoutTypeTree = new LayoutParamsTypeTree();
            ViewNode<View> node =
                    new ViewNode<>(
                            typeTree.typeOf(view), layoutTypeTree.typeOf(view.getLayoutParams()));
            node.readProperties(view);

            Resource layout = node.getLayoutResource(view);

            List<Property> viewProperties = node.getViewProperties();
            for (Property property : viewProperties) {
                attributes.put(property.getPropertyType().getName(),
                        String.valueOf(property.getValue()));
            }

            List<Property> layoutProperties = node.getLayoutProperties();
            for (Property property : layoutProperties) {
                attributes.put(property.getPropertyType().getName(),
                        String.valueOf(property.getValue()));
            }
        }

        return attributes;
    }

    /**
     * Returns a combined map with the basic, internal and layout inspector attributes.
     * @return a map of attributes.
     */
    public Map<String, String> getAllAttributes() {
        Map<String, String> attributes = new HashMap<>();

        attributes.putAll(getLayoutInspectorAttributes());
        attributes.putAll(getInternalViewAttributes());
        attributes.putAll(getBasicViewAttributes());

        return attributes;
    }
}
