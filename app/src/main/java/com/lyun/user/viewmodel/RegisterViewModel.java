package com.lyun.user.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.ObservableField;

import com.lyun.api.response.APIResult;
import com.lyun.library.mvvm.command.RelayCommand;
import com.lyun.library.mvvm.viewmodel.ViewModel;
import com.lyun.user.model.RegisterModel;

import net.funol.databinding.watchdog.annotations.WatchThis;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

/**
 * Created by ZHAOWEIWEI on 2017/1/16.
 */

public class RegisterViewModel extends ViewModel {

    public final ObservableField<String> username = new ObservableField<>("");
    public final ObservableField<String> password = new ObservableField<>("");
    public final ObservableField<String> comfirmPassword = new ObservableField<>("");

    @WatchThis
    public final BaseObservable onRegisterSuccess = new BaseObservable();
    @WatchThis
    public final ObservableField<Throwable> onRegisterFailed = new ObservableField<>();

    public RelayCommand onRegisterButtonClick = new RelayCommand(() -> {
//        if ("".equals(username.get())){
//        }else if ("".equals(password.get())){
//        }else if ("".equals(comfirmPassword.get())){
//        }else if (!(password.get()==comfirmPassword.get())){
//        }else{
//        }
        register(username.get(), password.get());
    });

    private void register(String username, String password) {
        new RegisterModel().register(username, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apiResult -> onRegisterSuccess.notifyChange(),
                        throwable -> onRegisterFailed.set(throwable));
    }
}
