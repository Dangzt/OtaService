package com.example.upgrading.presenter;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.upgrading.bean.UpdataBean;
import com.example.upgrading.view.DownloadService;
import com.example.upgrading.utils.OkHttpUtil;
import com.example.upgrading.utils.UpdataUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class UpdataPresenter implements UpdataContract.Presenter {
    private UpdataContract.View view;
    private UpdataBean updataBean;
    private boolean isForce;
    private boolean isIncremental;
    private String url;
    private String md5;
    private String version;
    private ServiceConnection conn;

    public UpdataPresenter(UpdataContract.View view) {
        this.view = view;
    }

    /**
     * 检测是否需要升级
     *
     * @param local
     */
    @Override
    public void checkUpdate(final String local) {
        OkHttpUtil.getInstance().getDataAsyn("https://ota.soundai.cn/yg/android/DeltaHear/update.json", new OkHttpUtil.NetCall() {
            @Override
            public void success(Call call, String response) throws IOException {
                Log.e("dang","当前线程"+Thread.currentThread());
                Gson gson = new Gson();
                updataBean = gson.fromJson(response, UpdataBean.class);
                int compareForce = UpdataUtils.compareVersion(local, updataBean.getData().getForceVersion());
                int compare = UpdataUtils.compareVersion(local, updataBean.getData().getVersion());
                if (compareForce == -1) {
                    Toast.makeText((Context) view, "需要去升级", Toast.LENGTH_LONG).show();
                    isForce = true;
                    isIncremental = isIncremental(updataBean, local);
                    version = updataBean.getData().getForceVersion();
                } else if (compare == -1) {
                    Toast.makeText((Context) view, "是否去升级", Toast.LENGTH_LONG).show();
                    isForce = false;
                    isIncremental = isIncremental(updataBean, local);
                    version = updataBean.getData().getVersion();
                } else {
                    Toast.makeText((Context) view, "不需要升级", Toast.LENGTH_LONG).show();
                    isForce = false;
                    isIncremental = false;
                    version = null;
                }
                if (!isIncremental && version != null) {
                    url = updataBean.getData().getFullUpgrade().getUrl();
                    md5 = updataBean.getData().getFullUpgrade().getMd5sum();
                }
                view.showUpdate(url, version, isForce, isIncremental, md5);

            }

            @Override
            public void failed(Call call, IOException e) {
                view.showFail("版本检测异常");
            }
        });
    }


    /**
     * 绑定service去升级
     *
     * @param context
     * @param url
     */
    @Override
    public void downApk(Context context, final String url) {
        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
                DownloadService downloadService = binder.getService();
                downloadService.downApk(url, new DownloadService.DownloadCallback() {
                    @Override
                    public void onPrepare() {
                        view.showPrepare("正在连接服务器");
                    }

                    @Override
                    public void onProgress(int progress) {
                        view.showProgress(progress);
                    }

                    @Override
                    public void onComplete(File file) {
                        view.showComplete(file);
                    }

                    @Override
                    public void onFail(String msg) {
                        view.showFail(msg);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent intent = new Intent(context, DownloadService.class);
        context.bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }

    /**
     * 解绑service
     *
     * @param context
     */
    @Override
    public void unbind(Context context) {
        context.unbindService(conn);
    }

    /**
     * 检测是否是增量升级
     *
     * @param updataBean
     * @param local
     * @return
     */
    private boolean isIncremental(UpdataBean updataBean, String local) {
        for (Object UpgradeBean : updataBean.getData().getIncrementalUpgrade()) {
            UpdataBean.DataBean.IncrementalUpgradeBean version = (UpdataBean.DataBean.IncrementalUpgradeBean) UpgradeBean;
            if (UpdataUtils.compareVersion(local, version.getSupportVersion()) == 0) {
                url = version.getUrl();
                md5 = version.getMd5sum();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
