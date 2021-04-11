package com.mycompany;

import com.alibaba.fastjson.JSON;import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.mycompany.Util.Utils;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import static com.mycompany.Util.CONST_VAL.*;
import static com.mycompany.Util.Utils.stringListToStringArray;

public class ZhToEn extends AnAction {
    private boolean isZhToEn = true;//是否是中译英
    private String selectResult = null;//最后选中的要替换源代码中的中文翻译

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
            selectedText = new String(model.getSelectedText().getBytes("gbk"),"utf-8");
            selectedText = model.getSelectedText();
            if(model.getSelectedText().matches("[\u4E00-\u9FA5]+"))
                isZhToEn = true;
            else isZhToEn = false;
            System.out.println("初始的selectedText字节数为:"+selectedText.getBytes().length);
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        }
        if (TextUtils.isEmpty(selectedText)) {
            return;
        }

        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        if(propertiesComponent.getValue(selectedText) != null) {//已经查询过一次
            if(isZhToEn){
                String[] translationArray = propertiesComponent.getValues(selectedText);
                zhToEn(translationArray);
                return;
            }else{
                String lastResult = propertiesComponent.getValue(selectedText);
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(lastResult, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                        .setFadeoutTime(100000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(mEditor), Balloon.Position.below);
                return;
            }
        }
        //post方式发送查询数据
        String result = "";
        PrintWriter out = null;
        BufferedReader in = null;
        try{
            //todo 开启线程异步获取数据
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
            String sign = Utils.SHA256(signStr);

            StringBuffer buffer = new StringBuffer();
            String s = "q="+selectedText;
            System.out.println("q=+selectedText 的长度为"+s.getBytes().length);
            buffer.append("q="+selectedText);
            buffer.append("&from=auto");
            buffer.append("&appKey="+YOUDAO_APP_KEY);
            buffer.append("&salt="+salt);
            buffer.append("&sign="+sign);
            buffer.append("&signType=v3");
            buffer.append("&curtime="+curTime);
            if(isZhToEn) buffer.append("&to=en");
            else buffer.append("&to=zh-CHS");

            connection.setDoOutput(true);
            connection.setDoInput(true);

            out =new PrintWriter(connection.getOutputStream());
            out.print(new String(buffer.toString().getBytes()));
            out.flush();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line ;
            while((line=in.readLine())!=null){
                result += " "+line;
            }

        }catch (Exception exception){
            System.out.println("发送post请求出现错误"+e);
        }finally {
            try {
                if(out != null) out.close();
                if(in != null) in.close();
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }

        try{
            if(result.equals("")){
                Messages.showMessageDialog(new String("请求数据失败了 ".getBytes(),"utf-8"), "Translation", Messages.getInformationIcon());
            }else{
                System.out.println("请求结果为"+result);
                YouDaoResult youDaoResult = JSON.parseObject(result, YouDaoResult.class);
                if (isZhToEn){
                    String[] translationArray = stringListToStringArray(youDaoResult.translation);
                    if(translationArray != null){
                        zhToEn(translationArray);
                        propertiesComponent.setValues(youDaoResult.query,translationArray);//保存查询后的英文数组
                        System.out.println(result);
                    }else {
                        Messages.showMessageDialog(new String("请求数据失败 结果为空".getBytes(),"utf-8"), "Translation", Messages.getInformationIcon());
                    }
                }else{
                    enToZh(selectedText,youDaoResult,mEditor);
                }
            }
        }catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        }
    }

    //显示英译中的结果
    public void enToZh(String selectedText,YouDaoResult youDaoResult,Editor mEditor){
        try {
            PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();//持久化保存数据
            StringBuffer zhResult = new StringBuffer();//TODO
            zhResult.append(new String("选中的文本为:".getBytes(), "utf-8"));
            zhResult.append(new String(selectedText.getBytes(), "utf-8"));
            zhResult.append(new String("\n\n基本翻译为：\n".getBytes(), "utf-8"));
            for (String item : youDaoResult.translation) {
                zhResult.append(item + "\n");
            }
            if (youDaoResult.web != null) {
                zhResult.append(new String("\n网络释义为：\n".getBytes(), "utf-8"));
                for (WebPart part : youDaoResult.web) {
                    zhResult.append(part.key + ":");
                    for (int webIndex = 0; webIndex < part.value.size(); webIndex++) {
                        if (webIndex != 0) zhResult.append("," + part.value.get(webIndex));
                        else zhResult.append(part.value.get(webIndex));
                    }
                    zhResult.append("\n");
                }
            }
            propertiesComponent.setValue(youDaoResult.query,zhResult.toString());//将查询到的英译中处理后保存起来
            JBPopupFactory factory = JBPopupFactory.getInstance();
            factory.createHtmlTextBalloonBuilder(zhResult.toString(), null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                    .setFadeoutTime(100000)
                    .createBalloon()
                    .show(factory.guessBestPopupLocation(mEditor), Balloon.Position.below);
        }catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        }
    }

    //显示中译英的结果，可以选中并替换
    public void zhToEn(String[] results){

        JFrame jf = new JFrame("Translation");
        JPanel panel = new JPanel();

        final JBList<String> list = new JBList<>();// 创建一个 JList 实例
        list.setPreferredSize(new Dimension(200, 100));// 设置一下首选大小
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);// 允许可间断的多选

        list.setListData(results);// 设置选项数据（内部将自动封装成 ListModel ）

        // 添加选项选中状态被改变的监听器
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] indices = list.getSelectedIndices();// 获取所有被选中的选项索引
                ListModel<String> listModel = list.getModel();// 获取选项数据的 ListModel
                // 输出选中的选项
                for (int index : indices) {
                    //todo 获取最后一个选中的翻译结果，填回源代码
                    selectResult = listModel.getElementAt(index);
                    System.out.println("选中: " + index + " = " + listModel.getElementAt(index));
                }
                System.out.println();
            }
        });

        list.setSelectedIndex(0);// 设置默认选中项
        panel.add(list);// 添加到内容面板容器
        jf.setContentPane(panel);
        jf.setVisible(true);//设置可见
        jf.setSize(300, 300);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);//设置关闭时的操作
    }
}
