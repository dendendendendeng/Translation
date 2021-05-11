package com.mycompany.Util;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.mycompany.YouDao.YouDaoResult;
import okhttp3.*;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;

import static com.mycompany.Util.CONST_VAL.*;
import static com.mycompany.Util.Popup.popup;
import static com.mycompany.Util.Popup.popupList;
import static com.mycompany.Util.Utils.isContainZh;
import static com.mycompany.Util.Utils.stringListToStringArray;

public class YouDaoRequest {
    private static final OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
    private static LinkedHashMap<String, YouDaoResult> map = HISTORY.getState();

    public static String youDaoRequest(String selectedString){
        String jsonResult = "";//要返回的json字符串

        Request request = new Request.Builder()//创建Request 对象。
                .url(YOUDAO_APP_URL)
                .post(builtFormBody(selectedString).build())//传递请求体
                .build();

        try {
            Response response = client.newCall(request).execute();
            jsonResult = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonResult;
    }

    public static void requestAndPopup(String selectedText,AnActionEvent event, TextRange textRange){
        Request request = new Request.Builder()
                .url(YOUDAO_APP_URL)
                .post(builtFormBody(selectedText).build())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    SwingUtilities.invokeAndWait(() -> Messages.showMessageDialog("网络请求出现错误！", "Information", Messages.getInformationIcon()));
                } catch (InterruptedException | InvocationTargetException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    try {
                        String result = response.body().string();
                        YouDaoResult youDaoResult = JSON.parseObject(result, YouDaoResult.class);
                        String[] translationArray = stringListToStringArray(youDaoResult.translation);
                        SwingUtilities.invokeAndWait(() -> popupList(translationArray,event,textRange));
                        if(map == null) map = new LinkedHashMap<>();
                        map.put(selectedText,youDaoResult);
                        HISTORY.loadState(map);
                    } catch (InterruptedException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        SwingUtilities.invokeAndWait(() -> Messages.showMessageDialog("网络请求出现错误！", "Information", Messages.getInformationIcon()));
                    } catch (InterruptedException | InvocationTargetException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        });
    }

    public static void requestAndReplace(String selectedString, Editor mEditor){
        Request request = new Request.Builder()//创建Request 对象。
                .url(YOUDAO_APP_URL)
                .post(builtFormBody(selectedString).build())//传递请求体
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    SwingUtilities.invokeAndWait(() -> Messages.showMessageDialog("网络请求出现错误！", "Information", Messages.getInformationIcon()));
                } catch (InterruptedException | InvocationTargetException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    try {String result = response.body().string();
                        YouDaoResult youDaoResult = JSON.parseObject(result, YouDaoResult.class);
                        if(map == null) map = new LinkedHashMap<>();
                        map.put(youDaoResult.query,youDaoResult);
                        HISTORY.loadState(map);
                        SwingUtilities.invokeAndWait(() -> popup(youDaoResult,mEditor));
                    } catch (InterruptedException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        SwingUtilities.invokeAndWait(() -> Messages.showMessageDialog("网络请求出现错误！", "Information", Messages.getInformationIcon()));
                    } catch (InterruptedException | InvocationTargetException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        });
    }

    public static FormBody.Builder builtFormBody(String selectedString){
        String salt = String.valueOf(System.currentTimeMillis());
        String curTime = String.valueOf(System.currentTimeMillis() / 1000);
        String signStr ;
        if(selectedString.length() <= 20) {
            signStr = YOUDAO_APP_KEY+selectedString+salt+curTime+YOUDAO_API_CODE;
        }else {
            String input = selectedString.substring(0,10)+selectedString.length()+selectedString.substring(selectedString.length()-10);
            signStr = YOUDAO_APP_KEY+input+salt+curTime+YOUDAO_API_CODE;
        }
        String sign = Utils.SHA256(signStr);

        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("q",selectedString);//传递键值对参数
        formBody.add("from","auto");
        formBody.add("appKey",YOUDAO_APP_KEY);
        formBody.add("salt",salt);
        formBody.add("sign",sign);
        formBody.add("signType","v3");
        formBody.add("curtime",curTime);
        if(isContainZh(selectedString)) formBody.add("to","en");
        formBody.add("to","zh-CHS");
        return formBody;
    }
}
