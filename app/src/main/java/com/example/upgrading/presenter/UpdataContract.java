package com.example.upgrading.presenter;

import android.content.Context;

import java.io.File;

public interface UpdataContract {
    interface View{
        void showUpdate(String url,String version,boolean isForce,boolean isIncremental,String md5);
        void showPrepare(String msg);
        void showProgress(int progress);
        void showFail(String msg);
        void showComplete(File file);
    }

    interface Presenter{
        void checkUpdate(String local);
        void downApk(Context context,String url);
        void unbind(Context context);
    }
}
