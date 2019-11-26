package com.example.upgrading.view;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;

import com.example.upgrading.R;
import com.example.upgrading.bean.UpdataBean;
import com.example.upgrading.utils.NetWorkUtils;
import com.example.upgrading.utils.OkHttpUtil;
import com.example.upgrading.utils.SharedPreferencesHelper;
import com.example.upgrading.utils.UpdataUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.lang.Thread.sleep;

public class DownloadService extends Service {
    private DownloadBinder binder = new DownloadBinder();
    DownloadCallback call;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    //定义notify的id，避免与其它的notification的处理冲突
    private static final int NOTIFY_ID = 0;
    private static final String CHANNEL = "update";
    //定义个更新速率，避免更新通知栏过于频繁导致卡顿
    private float rate = .0f;
    private NotificationChannel channel;
    private UpdataBean updataBean;
    private String name; //名称
    private ThreadPoolExecutor executor;// 线程池
    private MyThread thread;//线程
    private long downLoadSize;//下载文件的长度
    private String url;//下载路径
    private RandomAccessFile raf; // 读取写入IO方法
    private long total;
    private File file;
    private PendingIntent pIntent;

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public ComponentName startForegroundService(Intent service) {
        return super.startForegroundService(service);
    }

    @Override
    public void onCreate() {
        Log.e("dang", "onCreate++");
        initNotification();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("dang", "onStartCommand++");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 使用handler发送消息
                Message message = Message.obtain();
                message.what = 0;
                timerHandler.sendMessage(message);
            }
        }, 0, 43200000);
        return super.onStartCommand(intent, flags, startId);
    }

    private Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0) {
                checkUpdate();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("dang", "onBind++");
        // TODO: Return the communication channel to the service.
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("dang", "onUnbind++");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e("dang", "onDestroy++");
        //        if (thread != null) {
//            executor.remove(thread);
//            thread = null;
//        }
        mNotificationManager.cancelAll();
        super.onDestroy();
    }

    private void initNotification() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(this);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                channel = new NotificationChannel(CHANNEL, "com.example.upgrading", NotificationManager
                        .IMPORTANCE_MIN);
                mNotificationManager.createNotificationChannel(channel);
            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        pIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void checkUpdate() {
        if (NetWorkUtils.isNetworkConnected(this)) {
            final String localVersion = UpdataUtils.getLocalVersion(this);
            OkHttpUtil.getInstance().getDataAsyn("https://ota.soundai.cn/yg/android/DeltaHear/update.json", new OkHttpUtil.NetCall() {
                @Override
                public void success(Call call, String response) throws IOException {
                    Gson gson = new Gson();
                    updataBean = gson.fromJson(response, UpdataBean.class);
                    int compareForce = UpdataUtils.compareVersion(localVersion, updataBean.getData().getForceVersion());
                    int compare = UpdataUtils.compareVersion(localVersion, updataBean.getData().getVersion());
                    if (compareForce == -1) {
//                        Toast.makeText(DownloadService.this, "需要去升级", Toast.LENGTH_LONG).show();
                    } else if (compare == -1) {
//                        Toast.makeText(DownloadService.this, "是否去升级", Toast.LENGTH_LONG).show();
                    } else {
//                        Toast.makeText(DownloadService.this, "不需要升级", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void failed(Call call, IOException e) {
                    Toast.makeText(DownloadService.this, "版本检测异常", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(DownloadService.this, "请检查网络", Toast.LENGTH_LONG).show();
        }
    }

    private void versionNotification(String msg) {
        mBuilder.setContentTitle("版本更新")
                .setContentIntent(pIntent)
                .setContentText(msg)
                .setChannelId(CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
//        startForeground(1, mBuilder.build());
    }

    /**
     * 下载更新
     *
     * @param url
     * @param callback
     */
    public void downApk(String url, DownloadCallback callback) {
        this.url = url;
        this.call = callback;
        initDownload();
        if (thread == null) {
            thread = new MyThread();
            executor.execute(thread);
        } else {
            int threadCount = ((ThreadPoolExecutor) executor).getActiveCount();
            Log.e("dang", "当前线程数" + threadCount);
            Toast.makeText(DownloadService.this, "下载中！", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 初始化方法
     */
    private void initDownload() {
        this.name = "ota.zip";
        if (executor == null) {
            executor = new ThreadPoolExecutor(5, 5, 50, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3000));
        }
    }

    /**
     * 自定义线程
     */
    class MyThread extends Thread {
        @Override
        public void run() {
            super.run();
            downLoadFile();
        }
    }

    private void downLoadFile() {
        if (TextUtils.isEmpty(url)) {
            Message message = Message.obtain();
            message.what = 1;
            message.obj = "下载路径有误！";
            handler.sendMessage(message);
        }
        handler.sendEmptyMessage(0);
        try {
            total = getContentLength(url);
            Log.e("dang", "文件总大小" + total);
            String root = Environment.getDataDirectory().getPath();
            file = new File("/data/ota_package/", name);
            if (!file.exists()) {
                Log.e("dang", "创建文件");
                file.createNewFile();

                downLoadSize = file.length();// 文件的大小
                raf = new RandomAccessFile(file, "rwd");//实例化一下我们的RandomAccessFile()方法
            } else {
                downLoadSize = file.length();// 文件的大小
                Log.e("dang", "文件存在" + downLoadSize);
                if (downLoadSize == total) {// 判断是否下载完成
                    Message message = Message.obtain();
                    message.what = 3;
                    message.obj = file.getAbsoluteFile();
                    handler.sendMessage(message);
                    return;
                } else {
                    if (raf == null) {//判断读取是否为空
                        raf = new RandomAccessFile(file, "rwd");
                    }
                    raf.seek(downLoadSize);
                }
            }
            Response response = OkHttpUtil.getInstance().getData(url, downLoadSize, total);
            if (response != null) {
                InputStream ins = response.body().byteStream();
                int len = 0;
                byte[] by = new byte[1024];
                while ((len = ins.read(by)) != -1) {
                    raf.write(by, 0, len);
                    downLoadSize += len;
                    Log.e("TAG", "下载进度" + downLoadSize);
                    final int progress = (int) (downLoadSize * 1.0f / total * 100);
                    if (rate != progress) {
                        Message message = Message.obtain();
                        message.what = 2;
                        message.obj = progress;
                        handler.sendMessage(message);
                        rate = progress;
                    }
                }
                if (raf != null) {
                    raf.close();
                }
                Message message = Message.obtain();
                message.what = 3;
                message.obj = file.getAbsoluteFile();
                handler.sendMessage(message);
            } else {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = "下载失败";
                handler.sendMessage(message);
            }

        } catch (IOException e) {
            if (e.toString().equals("java.io.IOException: write failed: ENOSPC (No space left on device)")){
                Message message = Message.obtain();
                message.what = 1;
                message.obj = "磁盘已满";
                handler.sendMessage(message);
                Log.e("dang", "报错了" + e.toString());
            }
            Message message = Message.obtain();
            message.what = 1;
            message.obj = "下载出错" + e.toString();
            handler.sendMessage(message);
            Log.e("dang", "报错了" + e.toString());

            e.printStackTrace();
        }
    }

    //通过OkhttpClient获取文件的大小
    public long getContentLength(String url) throws IOException {
        Response response = OkHttpUtil.getInstance().getLength(url);
        long length = response.body().contentLength();
        return length;
    }

    /**
     * 把处理结果放回ui线程
     */
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    call.onPrepare();
                    break;
                case 1:
                    if (thread != null) {
                        executor.remove(thread);
                        thread = null;
                    }
                    call.onFail((String) msg.obj);
                    break;
                case 2: {
                    int progress = (int) msg.obj;
                    call.onProgress(progress);
                }
                break;
                case 3: {
                    if (file.length() == total) {
                        Toast.makeText(DownloadService.this, "下载完成！", Toast.LENGTH_LONG).show();
                        if (thread != null) {
                            executor.remove(thread);
                            thread = null;
                        }
                        call.onComplete((File) msg.obj);
                    }
                }
                break;
            }
            return false;
        }
    });


    public interface DownloadCallback {
        void onPrepare();

        void onProgress(int progress);

        void onComplete(File file);

        void onFail(String msg);
    }
}
