package com.example.user.downlandpdf;

/**
 * Created by Administrator on 2016/11/3.
 */
public interface ISupportOkHttp<E> {

    /**
     * 请求完成
     */
    public void requestCompleted(String task_name, E params);
    /**
     * 请求失败
     */
    public void requestEndedWithError(String task_name, String error);
    /**
     * 无返回
     */
    public void requestIntentWithError(String task_name, String error);
}
