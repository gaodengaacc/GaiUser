package com.lyun.library.mvvm.viewmodel;

import android.content.Context;
import android.databinding.ObservableField;

/**
 * @author Gordon
 * @since 2016/12/22
 * do(上传数据的Dialog ViewModel)
 */
public class ProgressDialogViewModel extends ViewModel {
    public final ObservableField<String> text = new ObservableField<>();
    public final ObservableField<Integer> maxProgress = new ObservableField<>();
    public final ObservableField<Integer> progress = new ObservableField<>();
    public ProgressDialogViewModel(Context context) {
        super(context);
        text.set("正在上传数据...");
        maxProgress.set(100);
        progress.set(0);
    }
}