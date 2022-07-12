package org.mate.commons.interaction.action.espresso;

import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.test.espresso.matcher.ViewMatchers;

import org.mate.commons.interaction.action.espresso.layout_inspector.common.Resource;
import org.mate.commons.interaction.action.espresso.layout_inspector.property.LayoutParamsTypeTree;
import org.mate.commons.interaction.action.espresso.layout_inspector.property.Property;
import org.mate.commons.interaction.action.espresso.layout_inspector.property.ViewNode;
import org.mate.commons.interaction.action.espresso.view_tree.EspressoViewTree;
import org.mate.commons.interaction.action.espresso.layout_inspector.property.ViewTypeTree;
import org.mate.commons.utils.MATELog;

import java.lang.reflect.Field;
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
     * Returns the attributes of the wrapped view that are found in the instance itself.
     * @return a map of attributes.
     */
    public Map<String, String> getBasicViewAttributes() {
        Map<String, String> attributes = new HashMap<>();

        attributes.put("x", String.valueOf(view.getX()));
        attributes.put("y", String.valueOf(view.getY()));
        attributes.put("width", String.valueOf(view.getWidth()));
        attributes.put("height", String.valueOf(view.getHeight()));

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
     * Returns a combined map with the basic attributes and the layout inspector attributes.
     * @return a map of attributes.
     */
    public Map<String, String> getAllAttributes() {
        Map<String, String> attributes = new HashMap<>();

        attributes.putAll(getLayoutInspectorAttributes());
        attributes.putAll(getBasicViewAttributes());

        attributes.put("text", getText());
        attributes.put("contentDescription", getContentDescription());

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
}
