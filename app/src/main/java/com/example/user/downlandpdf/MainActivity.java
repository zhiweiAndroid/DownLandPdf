package com.example.user.downlandpdf;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.user.downlandpdf.service.UpdateService;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import java.io.File;
import okhttp3.Call;
import rx.Observer;
import static com.zhy.http.okhttp.log.LoggerInterceptor.TAG;

public class MainActivity extends AppCompatActivity implements ISupportOkHttp{

    public static final String url="http://snfqitfc.boyuanfinancial.com/sinafenqi-interface/customer/html/agreement/pdf/yianInsurance2.pdf";
    private String netversion="1.0.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv1 = findViewById(R.id.tv1);
        String appVersionName = getAppVersionName(this);
        tv1.setText(appVersionName);
        Button  btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              initPermission();
            }
        });

        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent(MainActivity.this, UpdateService.class);
                service.putExtra("INTENT_KEY_VERSION", netversion);
                startService(service);
            }
        });

    }

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    private void initPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean){
                            final String path = getPdfPath();
                            downLandPdf("pdf222.pdf",path,url);
                        } else {

                        }
                    }
                });
    }

    private void downLandPdf(String filename, String filepath, final String url) {
        OkHttpUtils.get()
                .url(url)
                .build()
                .execute(new FileCallBack(filepath,
                        filename) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError :" + e.getMessage());
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
                        //super.inProgress(progress, total, id);

                        Log.e(TAG,"inProgress"+(int)(100*progress));
                    }

                    @Override
                    public void onResponse(File file, int id) {
                        Log.e(TAG, "onResponse :" + file.getAbsolutePath());
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                        Uri uri;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(getApplicationContext(), Constant.FILE_PROVIDER, file);
                        }else {
                            uri = Uri.fromFile(file);
                        }
                        Log.e(TAG, "onResponse >>>>>>>>" + uri);
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        i.setDataAndType(uri, "application/pdf");
                        try {
                            startActivity(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                });


    }

    @Override
    public void requestCompleted(String task_name, Object params) {

    }

    @Override
    public void requestEndedWithError(String task_name, String error) {

    }

    @Override
    public void requestIntentWithError(String task_name, String error) {

    }


    /**
     * 获取pdf保存路径
     * @return
     */
    private String  getPdfPath(){
        return  FileUtils.externalRootDirectory(MainActivity.this)+File.separator+System.currentTimeMillis();
    }
}
