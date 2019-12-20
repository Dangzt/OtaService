package com.example.upgrading.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.upgrading.bean.EventType;
import com.example.upgrading.presenter.UpdataContract;
import com.example.upgrading.presenter.UpdataPresenter;
import com.example.upgrading.utils.FileMD5Utils;
import com.example.upgrading.utils.LedUtils;
import com.example.upgrading.utils.NetWorkUtils;
import com.example.upgrading.utils.OkHttpUtil;
import com.example.upgrading.R;
import com.example.upgrading.bean.UpdataBean;
import com.example.upgrading.utils.PermissionsUtils;
import com.example.upgrading.utils.UpdataUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Response;

import static android.os.Process.killProcess;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements UpdataContract.View {
    private static final String TAG = "MainActivity";
    //需要的权限
    final String[] permiss = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
    //    private static ProgressDialog progressDialog;
    private UpdataPresenter updataPresenter;
    private TextView tvLocalVersion;
    private TextView tvNewVersion;
    private RelativeLayout rlVersion;
    private String localVersion;
    private boolean isForce;
    private boolean isIncremental;
    private String url;
    private String version;
    private String md5;
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    public static final String EXTRA_KEY_CONFIRM = "android.intent.extra.KEY_CONFIRM";
    private ImageView imBack;
    private TextView tvProgress;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("dang", "AonCreate++");
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        initView();
        updataPresenter = new UpdataPresenter(this);
        localVersion = UpdataUtils.getLocalVersion(this);
        tvLocalVersion.setText(localVersion);
        if (NetWorkUtils.isNetworkConnected(this)) {
            updataPresenter.checkUpdate(localVersion);
        } else {
            Toast.makeText(MainActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
        }

        rlVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isForce && isIncremental) {
//                    forceProgress(url, version);
//                } else if (isForce && !isIncremental) {
//                    forceProgress(url, version);
//                } else if (!isForce && isIncremental) {
//                    nomalProgress(url, version);
//                } else {
//                    nomalProgress(url, version);
//                }
                updataPresenter.downApk(MainActivity.this, url);
            }
        });

        imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void initView() {
        tvLocalVersion = findViewById(R.id.tv_localVersion);
        tvNewVersion = findViewById(R.id.tv_newVersion);
        rlVersion = findViewById(R.id.rl_version);
        imBack = findViewById(R.id.im_back);
        tvProgress = findViewById(R.id.tv_progress);
    }

    @Override
    public void showUpdate(final String url, String version, boolean isForce, boolean isIncremental, String md5) {
//        progressDialog = new ProgressDialog(this);
        if (version != null) {
            rlVersion.setVisibility(View.VISIBLE);
            tvNewVersion.setText("检测到新版本" + version);
            tvProgress.setText(" ");
            this.isForce = isForce;
            this.isIncremental = isIncremental;
            this.url = url;
            this.version = version;
            this.md5 = md5;
        } else {
            rlVersion.setVisibility(View.GONE);
            tvNewVersion.setText("当前版本为最新版本");
        }
    }

    @Override
    public void showPrepare(String msg) {
        tvProgress.setText(msg);
    }


    @Override
    public void showProgress(int progress) {
        tvProgress.setText(progress + "%");
    }

    @Override
    public void showFail(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        tvProgress.setText("下载出错");
    }

    @Override
    public void showComplete(File file) {
        Toast.makeText(this, "下载到" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        if (FileMD5Utils.getFileMD5(file).equals(md5)) {
            tvProgress.setText("下载完成");
            LedUtils.sendEvent(this,LedUtils.EVENT_ACTION_REMOVE, EventType.DOWNLOADING_COMPLETE.name());
            try {
                //升级
                installPackage(this, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "文件下载错误，请重新下载", Toast.LENGTH_LONG).show();
            tvProgress.setText("下载错误");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public void installPackage(Context context, File packageFile)
            throws IOException {
//        String filename = packageFile.getCanonicalPath();
//        final String filenameArg = "--update_package=" + filename;
//        bootCommand(context, filenameArg);
        RecoverySystem.installPackage(context, packageFile);
    }

    private void bootCommand(Context context, String args) throws IOException {
        RECOVERY_DIR.mkdirs();  // In case we need it
        COMMAND_FILE.delete();  // In case it's not writable
        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            if (!TextUtils.isEmpty(args)) {
                command.write(args);
                command.write("\n");
            }
        } finally {
            command.close();
        }
        // Having written the command file, go ahead and reboot
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        // 调用PowerManager类中的reboot方法
        pm.reboot("recovery");
        throw new IOException("Reboot failed (no permissions?)");
    }

    @Override
    protected void onDestroy() {
        Log.e("dang", "AonDestroy++");
        super.onDestroy();
    }

    private void checkPermissions() {
        PermissionsUtils.getInstance().chekPermissions(this, permiss, new PermissionsUtils.IPermissionsResult() {
            @Override
            public void passPermissons() {
                updataPresenter.checkUpdate(localVersion);
            }

            @Override
            public void forbitPermissons() {
                Toast.makeText(MainActivity.this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void nomalProgress(final String url, String version) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("检测到有新版本");
        progressDialog.setMessage("更新版本版本:" + version);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);//点击返回键，禁止退出
        progressDialog.setMax(100);
        progressDialog.setButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.cancel();
            }
        });
        progressDialog.show();
    }

    private void forceProgress(final String url, String version) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("检测到有新版本");
        progressDialog.setMessage("更新版本版本:" + version);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);//点击返回键，禁止退出
        progressDialog.setMax(100);
        progressDialog.show();
    }
}
