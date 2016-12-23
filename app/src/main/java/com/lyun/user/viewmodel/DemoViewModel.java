package com.lyun.user.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.lyun.user.BR;
import com.lyun.viewmodel.BaseViewModel;


public class DemoViewModel extends BaseViewModel {
    private String username;
    private String nickname;
    private int age;

    private String userIcon;

    public DemoViewModel() {
    }
    // get方法中添加@Bindable 否则set中无法notifyPropertyChanged中的BR
    // 添加完以后 Make Project(CTRL + F9) 否则BR找不到
    @Bindable
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        notifyPropertyChanged(BR.age);
    }
    @Bindable
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        notifyPropertyChanged(BR.nickname);
    }
    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    public DemoViewModel(int age, String nickname, String username, String userIcon) {
        this.age = age;
        this.nickname = nickname;
        this.username = username;
        this.userIcon = userIcon;
    }
    @Bindable
    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
        notifyPropertyChanged(BR.userIcon);
    }
}