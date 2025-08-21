package com.etgames.alarmme;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.view.ContextThemeWrapper;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class Helperr {

    private final Button btnAdd;
    private final Button btnRemove;
    private int lastIndex = -1;
    private final int maxItems;

    /**
     * @param _btnAdd    Button to add new input
     * @param _btnRemove Button to remove last input
     * @param maxItems   Maximum number of input fields allowed
     */
    public Helperr(Button _btnAdd, Button _btnRemove, int maxItems) {
        this.btnAdd = _btnAdd;
        this.btnRemove = _btnRemove;
        this.maxItems = maxItems;
        btnRemove.setEnabled(false);
    }

    /** Create a new TextInputLayout containing a TextInputEditText */
    private TextInputLayout createTextInputLayout(Context context, String hint, String data) {
        // Wrap context with your custom theme
        ContextThemeWrapper themedContext = new ContextThemeWrapper(context, R.style.Base_Theme_ALARMAPP);

        // Create TextInputLayout using themed context
        TextInputLayout layout = new TextInputLayout(themedContext);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Create TextInputEditText using themed context
        TextInputEditText editText = new TextInputEditText(themedContext);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        editText.setHint(hint);
        editText.setText(data);

        layout.addView(editText);
        return layout;
    }

    /**
     * Add a new TextInputLayout to container and list
     *
     * @param context    Context
     * @param layoutsList List of TextInputLayouts to track
     * @param container  LinearLayout container
     * @param hint       Hint text for EditText
     * @param data       Pre-filled text (can be null)
     */
    public void addOne(Context context, ArrayList<TextInputLayout> layoutsList, LinearLayout container, String hint, String data) {
        // Check last input if exists
        if (lastIndex >= 0) {
            TextInputLayout lastLayout = layoutsList.get(lastIndex);
            if (lastLayout != null) {
                EditText lastEdit = lastLayout.getEditText();
                // If getEditText() is null, create one
                if (lastEdit == null) {
                    lastEdit = new TextInputEditText(context);
                    lastLayout.addView(lastEdit);
                }

                // Check if empty
                if (lastEdit.getText() == null || lastEdit.getText().toString().trim().isEmpty()) {
                    lastEdit.setError("Can't be empty");
                    return;
                }

                // Disable last edit
                lastEdit.setEnabled(false);
            }
        }

        // Create new layout
        TextInputLayout layout = createTextInputLayout(context, hint, data);

        // Ensure new layout has an EditText
        if (layout.getEditText() == null) {
            TextInputEditText newEdit = new TextInputEditText(context);
            if (data != null) newEdit.setText(data);
            newEdit.setHint(hint);
            layout.addView(newEdit);
        }

        // Add to container and list
        container.addView(layout);
        layoutsList.add(layout);
        lastIndex = layoutsList.size() - 1;

        updateButtons();
    }

    /**
     * Remove the last TextInputLayout from container and list
     *
     * @param layoutsList List of TextInputLayouts
     * @param container   LinearLayout container
     */
    public void removeOne(ArrayList<TextInputLayout> layoutsList, LinearLayout container) {
        if (lastIndex < 0) return;

        TextInputLayout layoutToRemove = layoutsList.get(lastIndex);
        container.removeView(layoutToRemove);
        layoutsList.remove(lastIndex);
        lastIndex = layoutsList.size() - 1;

        if (lastIndex >= 0) {
            TextInputEditText lastEdit = (TextInputEditText) layoutsList.get(lastIndex).getEditText();
            if (lastEdit != null) lastEdit.setEnabled(true);
        }

        updateButtons();
    }

    /** Update Add/Remove buttons state */
    private void updateButtons() {
        btnRemove.setEnabled(lastIndex >= 0);
        btnAdd.setEnabled(lastIndex < maxItems - 1);
    }
}
