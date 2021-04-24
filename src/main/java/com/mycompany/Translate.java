package com.mycompany;

import com.alibaba.fastjson.JSON;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.JBColor;
import com.mycompany.Util.YouDaoRequest;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class Translate extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
        if (null == mEditor) {
            return;
        }

        //获取选中的文本 
        SelectionModel model = mEditor.getSelectionModel();
        String selectedText = model.getSelectedText();
        if (TextUtils.isEmpty(selectedText)) {
            return;
        }

        System.out.println(selectedText);
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        if(propertiesComponent.getValue(selectedText) != null) {//已经查询过一次
            JBPopupFactory factory = JBPopupFactory.getInstance();
            String lastResult = propertiesComponent.getValue(selectedText);
            assert lastResult != null;
            factory.createHtmlTextBalloonBuilder(lastResult, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                    .setFadeoutTime(100000)
                    .createBalloon()
                    .show(factory.guessBestPopupLocation(mEditor), Balloon.Position.below);
            return;
        }

        //post同步获取数据
        String result = YouDaoRequest.youDaoRequest(selectedText);
        System.out.println("请求结果为 "+result);
        YouDaoResult youDaoResult = JSON.parseObject(result, YouDaoResult.class);
        popup(selectedText,youDaoResult,mEditor);
    }

    //显示英译中的结果
    public void popup(String selectedText, YouDaoResult youDaoResult, Editor mEditor){
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();//持久化保存数据
        StringBuilder zhResult = new StringBuilder();
        zhResult.append("选中的文本为:");
        zhResult.append(selectedText);
        zhResult.append("\n\n基本翻译为：\n");
        if(youDaoResult.translation == null) {
            Messages.showMessageDialog("Translation字段为空", "Translation", Messages.getInformationIcon());
            return ;
        }
        for (String item : youDaoResult.translation) {
            zhResult.append(item).append("\n");
        }
        if (youDaoResult.web != null) {
            zhResult.append("\n网络释义为：\n");
            for (WebPart part : youDaoResult.web) {
                zhResult.append(part.key).append(":");
                for (int webIndex = 0; webIndex < part.value.size(); webIndex++) {
                    if (webIndex != 0) zhResult.append(",").append(part.value.get(webIndex));
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
    }
}
