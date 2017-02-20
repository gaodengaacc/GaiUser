package com.lyun.library.mvvm.bindingadapter.edittext;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.lyun.library.mvvm.command.RelayCommand;

public final class ViewBindingAdapter {
    @BindingAdapter("transformMethod")
    public static void setInputType(EditText editText, TransformationMethod method) {
        editText.setTransformationMethod(method);//设置密码显隐
        editText.setSelection(editText.getText().length());//设置光标位置
    }

    @android.databinding.BindingAdapter({"requestFocus"})
    public static void requestFocusCommand(EditText editText, final Boolean needRequestFocus) {
        if (needRequestFocus) {
            editText.setFocusableInTouchMode(true);
            editText.setSelection(editText.getText().length());
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            editText.setEnabled(false);
            editText.setEnabled(true);
        }

    }


    @android.databinding.BindingAdapter(value = {"beforeTextChangedCommand", "onTextChangedCommand", "afterTextChangedCommand"}, requireAll = false)
    public static void editTextCommand(EditText editText,
                                       final RelayCommand<TextChangeDataWrapper> beforeTextChangedCommand,
                                       final RelayCommand<TextChangeDataWrapper> onTextChangedCommand,
                                       final RelayCommand<String> afterTextChangedCommand) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (beforeTextChangedCommand != null) {
                    beforeTextChangedCommand.execute(new TextChangeDataWrapper(s, start, count, count));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (onTextChangedCommand != null) {
                    onTextChangedCommand.execute(new TextChangeDataWrapper(s, start, before, count));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (afterTextChangedCommand != null) {
                    afterTextChangedCommand.execute(s.toString());
                }
            }
        });
    }

    public static class TextChangeDataWrapper {
        public CharSequence s;
        public int start;
        public int before;
        public int count;

        public TextChangeDataWrapper(CharSequence s, int start, int before, int count) {
            this.s = s;
            this.start = start;
            this.before = before;
            this.count = count;
        }
    }


}

