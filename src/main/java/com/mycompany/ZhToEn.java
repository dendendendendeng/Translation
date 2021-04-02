package com.mycompany;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import com.mycompany.Util.SHAUtil;
import org.apache.http.util.TextUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import static com.mycompany.Util.CONST_VAL.*;

public class ZhToEn extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
        if (null == mEditor) {
            return;
        }

        //获取选中的文本 
        SelectionModel model = mEditor.getSelectionModel();
        String selectedText = null;
        try {
            selectedText = new String(model.getSelectedText().getBytes("utf-8"));
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        }
        if (TextUtils.isEmpty(selectedText)) {
            return;
        }

        //post方式发送查询数据
        String result = "";
        PrintWriter out = null;
        BufferedReader in = null;
        try{
            URL realUrl = new URL(YOUDAO_APP_URL);
            URLConnection connection = realUrl.openConnection();

            connection.setConnectTimeout(5 * 1000);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // 设置内容类型

            String salt = String.valueOf(System.currentTimeMillis());
            String curTime = String.valueOf(System.currentTimeMillis() / 1000);
            String signStr = YOUDAO_APP_KEY+selectedText+salt+curTime+YOUDAO_API_CODE;
            String sign = SHAUtil.SHA256(signStr);

            StringBuffer buffer = new StringBuffer();
            buffer.append("q="+selectedText);
            buffer.append("&from=auto");
            buffer.append("&appKey="+YOUDAO_APP_KEY);
            buffer.append("&salt="+salt);
            buffer.append("&sign="+sign);
            buffer.append("&to=en");
            buffer.append("&signType=v3");
            buffer.append("&curtime="+curTime);

            connection.setDoOutput(true);
            connection.setDoInput(true);

            out =new PrintWriter(connection.getOutputStream());
            out.print(buffer.toString());
            out.flush();
            System.out.println("请求参数为："+buffer.toString());
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line ;
            while((line=in.readLine())!=null){
                result += " "+line;
            }

        }catch (Exception exception){
            System.out.println("发送post请求出现错误"+e);
        }finally {
            try {
                if(out != null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }

        if(result.equals("")){
            Messages.showMessageDialog("请求数据失败了。。", "Translation", Messages.getInformationIcon());
        }else{
            YouDaoResult youDaoResult = JSON.parseObject(result, YouDaoResult.class);
            Messages.showMessageDialog("\n"+youDaoResult.translation.toString()+"\n", "Translation", Messages.getInformationIcon());
        }

        //解析json转成object，然后展示给用户

    }
}
