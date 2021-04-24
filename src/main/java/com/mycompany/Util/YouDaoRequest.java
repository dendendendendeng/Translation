package com.mycompany.Util;

import okhttp3.*;

import java.io.IOException;

import static com.mycompany.Util.CONST_VAL.*;
import static com.mycompany.Util.Utils.isContainZh;

public class YouDaoRequest {

    public static String youDaoRequest(String selectedString){
        String jsonResult = "";//要返回的json字符串
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。

        Request request = new Request.Builder()//创建Request 对象。
                .url(YOUDAO_APP_URL)
                .post(builtFormBody(selectedString).build())//传递请求体
                .build();

        System.out.println("request.body()"+request.body());

        try {
            Response response = client.newCall(request).execute();
            jsonResult = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                jsonResult = new String();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if(response.isSuccessful()){
//                    jsonResult = response.body().string();
//                }else {
//                    jsonResult = response.body().string();
//                }
//            }
//        });
        return jsonResult;
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
