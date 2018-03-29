package com.example.user.downlandpdf;

import android.os.Environment;
import android.util.Log;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;
import java.io.File;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Response;

import static com.zhy.http.okhttp.log.LoggerInterceptor.TAG;

/**
 * Created by Administrator on 2016/11/2.
 */
public class OKUtils {


    public static void downloadFile(String filename, File filepath, final String task_name, final String url, final ISupportOkHttp<String> iSupportOkHttp) {
        OkHttpUtils.get()
                .url(url)
                .build()
                .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "okHttp-test.mp4") {
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
                    }
                });

    }


    abstract class MyStringCallback extends StringCallback {
        public String parseNetworkResponse(Response response) throws IOException {
            String myresponse = response.body().string();
            if (response.code() == 200) {
                return myresponse;
            } else return null;
        }
    }
}
     


