package com.etgames.alarmme;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

//this class is a workaround to use non final variable inside the lambda expression
//it helped organising the code tho :)
public class Helperr {
    EditText last;
    int lastIndex;
    Button btnAdd;
    Button btnRemove;

    public Helperr(Button _btnAdd, Button _btnRemove) {
        btnAdd = _btnAdd;
        btnRemove = _btnRemove;
    }


    EditText creatEditText(Context context, String data) {
        EditText newEditText = new EditText(context);
        newEditText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        newEditText.setHint("add a new one");
        newEditText.setText(data);
        return newEditText;
    }

    public void addOne(Context context, ArrayList<EditText> TextsList, LinearLayout container, String data) {
        boolean firstOne = false;
        if (TextsList.isEmpty()) {

            last = creatEditText(context, data);
            container.addView(last);
            TextsList.add(last);
            lastIndex = 0;
            firstOne = true;
            btnRemove.setEnabled(false);
            if (data != null) {
                return;
            }
        }
        last = TextsList.get(lastIndex);
        if (last.getText().toString().trim().isEmpty()) {
            if (!firstOne) {

                last.setError("can't be empty");
            }
            return;
        }
        last.setEnabled(false);
        change(1);
        last = creatEditText(context, data);
        container.addView(last);
        TextsList.add(last);
    }

    public void removeOne(ArrayList<EditText> TextsList, LinearLayout container) {
        if (lastIndex > 0) {

            TextsList.remove(last);
            container.removeView(last);
            change(-1);
            last = TextsList.get(lastIndex);
            last.setEnabled(true);
        }
    }

    public void change(int value) {
        lastIndex += value;

        switch (lastIndex) {
            case 0:
                btnRemove.setEnabled(false);
                break;
            case 1:
                btnRemove.setEnabled(true);
                break;
            case 4:
                btnAdd.setEnabled(true);
                break;
            case 5:
                btnAdd.setEnabled(false);
                break;
            default:
                break;
        }


    }
}
