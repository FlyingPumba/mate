package org.mate.accessibility.check.widgetbased;

import org.mate.MATE;
import org.mate.accessibility.AccessibilityViolation;
import org.mate.accessibility.AccessibilityViolationTypes;
import org.mate.state.IScreenState;
import org.mate.ui.Widget;

import java.util.ArrayList;
import java.util.List;

//
public class FormControlLabelCheck implements IWidgetAccessibilityCheck {

    /*All form controls, such as text inputs, check boxes, select lists, or buttons,
    must each have a unique label. The label can be either a default value of the control,
    such as a submit button, or a correctly associated property or element, such as a label.
    While placeholders may provide additional hints, they are temporary and must not substitute a label.
    Labels must be visible and available to assistive technology.
     */

    private List<String> labeledBy;

    public FormControlLabelCheck(){

    }

    private boolean applicable(Widget widget){

        boolean buttonType = widget.isButtonType();
        boolean imageButtonType = widget.isImageButtonType();
        boolean imageSwitcherType = widget.isImageSwitcherType();
        boolean imageType = widget.isImageType();
        boolean spinnerType = widget.isSpinnerType();
        boolean editType = 	widget.isEditable();
        boolean textViewType = widget.isTextViewType();

        if (buttonType || imageButtonType || imageSwitcherType || imageType || spinnerType || editType || textViewType){
            if (widget.isImportantForAccessibility())
                return true;
        }


        //if (widget.isClickable() || widget.isEditable() || widget.isCheckable())
        //   return true;

        return false;
    }

    @Override
    public AccessibilityViolation check(IScreenState state, Widget widget) {

        labeledBy = new ArrayList<String>();
        for (Widget w: state.getWidgets()){
            labeledBy.add(w.getLabelFor());
        }

        if (!applicable(widget)){
            return null;
        }

        if (widget.isButtonType() || widget.isTextViewType()){
            if (!widget.getText().equals("")){
                return null;
            }
        }

        if (!widget.getHint().equals("")){
            return null;
        }

        //covered by EditableContentDesc
        if (!widget.getContentDesc().equals("")){
            if (!widget.isEditable())
                return null;
            else
                return new AccessibilityViolation(AccessibilityViolationTypes.EDITABLE_CONTENT_DESC,widget,state,"");
        }

        if (!widget.getLabeledBy().equals("")) {
            return null;
        }

        if (labeledBy.contains(widget.getResourceID())) {
            return null;
        }

        if (widget.isImageType() || widget.isImageSwitcherType()){
            return new AccessibilityViolation(AccessibilityViolationTypes.MISSING_ALTERNATIVE_TEXT,widget,state,"");
        }

        return new AccessibilityViolation(AccessibilityViolationTypes.MISSING_FORM_CONTROL_LABEL,widget,state,"");
    }

}
