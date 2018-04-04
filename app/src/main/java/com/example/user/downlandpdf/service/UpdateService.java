package com.example.user.downlandpdf.service;

/**
 * Created by Kodulf on 2016/6/18.
 */

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.widget.Toast;


import com.example.user.downlandpdf.BuildConfig;
import com.example.user.downlandpdf.Constant;
import com.example.user.downlandpdf.DownloadEvent;
import com.example.user.downlandpdf.L;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * 检测安装更新文件的助手类
 *
 * @author G.Y.Y
 */

public class UpdateService extends Service {
    private DownloadManager manager;
    private DownloadCompleteReceiver receiver;
    private DownloadManager.Request request;
    private String targetVersion;
    private long apkDownLoadId;
    private File apkFile;



    @Override
    public void onCreate() {
        super.onCreate();
        apkFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "pdf.apk");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        targetVersion = intent.getStringExtra("INTENT_KEY_VERSION");
        L.d("debug", "UpdateService-onStartCommand");
        if (needDownload()) {
            startDownLoad();
            return super.onStartCommand(intent, flags, startId);
        }

        installApk();

        return super.onStartCommand(intent, flags, startId);
    }

    private void initDownLoad() {
        manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        //设置下载地址
        request = new DownloadManager.Request(Uri.parse(Constant.APP_URL));

        // 设置允许使用的网络类型，这里是移动网络和wifi都可以
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);

        // 下载时，通知栏显示途中
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        }

        //设置下载文件的类型
        request.setMimeType("application/vnd.android.package-archive");

        //显示下载界面
        request.setVisibleInDownloadsUi(true);

        //设置下载后文件存放的位置
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "pdf.apk");

        request.setTitle("新浪分期正在更新...");


        receiver = new DownloadCompleteReceiver();
        //注册下载广播
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void startDownLoad() {

        if (manager == null) {
            initDownLoad();
        }

        if (apkFile.exists()) {
            apkFile.delete();
        }

        //将下载请求放入队列
        apkDownLoadId = manager.enqueue(request);

        if (BuildConfig.DEBUG) {
            //更新下载进度
//            updateDownloadStatus(apkDownLoadId);
        }
    }

    /**
     * 是否需要下载APK
     *
     * @return
     */
    private boolean needDownload() {
        PackageInfo info = getApplicationContext().getPackageManager().getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);

        if (info == null) {
            return true;
        }

        if (!info.packageName.equals(getApplicationContext().getPackageName())) {
            return true;
        }

        if (targetVersion != null) {
            if (!info.versionName.equals(targetVersion)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // 注销下载广播
        if (receiver != null) {
            unregisterReceiver(receiver);
        }

        super.onDestroy();
    }



    private void updateDownloadStatus(final long apkDownLoadId) {
        DownloadEvent event = new DownloadEvent(DownloadEvent.DOWNLOAD_TYPE_APK, apkDownLoadId, "新浪分期提货更新");
        event.setFirst(true);
        EventBus.getDefault().post(event);
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(apkDownLoadId));

                if (cursor == null) {
                    return;
                }

                if (!cursor.moveToFirst()) {
                    return;
                }

                long downloadSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                long totalSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DOWNLOAD_TYPE_APK, apkDownLoadId, "用户端更新").setStatus(status).setProgress((float) downloadSize / totalSize));

                switch (status) {
                    case DownloadManager.STATUS_FAILED:
                        Toast.makeText(getApplicationContext(), "更新包下载失败", Toast.LENGTH_SHORT);
                    case DownloadManager.STATUS_SUCCESSFUL:
                        return;
                }

                handler.postDelayed(this, 200);
            }
        };

        handler.postDelayed(runnable, 200);
    }

    class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                return;
            }

            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (downloadId == apkDownLoadId) {
                    installApk();
                }
            }

            //停止服务并关闭广播
            UpdateService.this.stopSelf();
        }
    }

    private void installApk() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);

        Uri uri = Uri.fromFile(apkFile);

        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(getApplicationContext(), Constant.FILE_PROVIDER, apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }
}
