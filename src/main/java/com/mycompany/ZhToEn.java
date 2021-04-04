package com.mycompany;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.mycompany.Util.SHAUtil;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

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
            buffer.append("&to=zh-CHS");
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
//            Messages.showMessageDialog("\n"+youDaoResult.translation.toString()+"\n"+youDaoResult.basic.explains.toString(), "Translation", Messages.getInformationIcon());
            JFrame jf = new JFrame("Test");
            jf.setSize(300, 300);
            jf.setLocationRelativeTo(null);
            jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            JPanel panel = new JPanel();

            // 创建一个 JList 实例
            final JBList<String> list = new JBList<>();

            // 设置一下首选大小
            list.setPreferredSize(new Dimension(200, 100));

            // 允许可间断的多选
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            int length = youDaoResult.translation.size();
            String[] results = new String[length];
            for(int i=0;i<length;i++){
                results[i] = youDaoResult.translation.get(i);
            }

            String apple = "苹果";
            String[] testStrings = new String[3];
            try {
                testStrings[0] = new String(apple.getBytes("gbk"),"UTF-8");
                testStrings[1] = new String("雪梨".getBytes("gbk"),"UTF-8");
                testStrings[2] = new String("香蕉".getBytes("gbk"),"UTF-8");
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                unsupportedEncodingException.printStackTrace();
            }
            // 设置选项数据（内部将自动封装成 ListModel ）
            list.setListData(testStrings);

            // 添加选项选中状态被改变的监听器
            list.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    // 获取所有被选中的选项索引
                    int[] indices = list.getSelectedIndices();
                    // 获取选项数据的 ListModel
                    ListModel<String> listModel = list.getModel();
                    // 输出选中的选项
                    for (int index : indices) {
                        System.out.println("选中: " + index + " = " + listModel.getElementAt(index));
                    }
                    System.out.println();
                }
            });

            // 设置默认选中项
            list.setSelectedIndex(0);

            // 添加到内容面板容器
            panel.add(list);

            jf.setContentPane(panel);
            jf.setVisible(true);
            jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }

        //解析json转成object，然后展示给用户
    }
}
