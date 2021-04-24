package com.mycompany;

import com.alibaba.fastjson.JSON;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.mycompany.Util.YouDaoRequest;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static com.mycompany.Util.Utils.isContainZh;
import static com.mycompany.Util.Utils.stringListToStringArray;

public class Replace extends AnAction {
    private String selectResult = null;//最后选中的要替换源代码中的中文翻译

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
        //是否是中译英
        boolean isZhToEn = isContainZh(selectedText);

        System.out.println(selectedText);
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        TextRange textRange = new TextRange(model.getSelectionStart(),model.getSelectionEnd());
        if(propertiesComponent.getValue(selectedText) != null) {//已经查询过一次
            JBPopupFactory factory = JBPopupFactory.getInstance();
            if(isZhToEn){
                String[] translationArray = propertiesComponent.getValues(selectedText);
                zhToEn(translationArray,e,textRange);
            }else{
                String lastResult = propertiesComponent.getValue(selectedText);
                assert lastResult != null;
                factory.createHtmlTextBalloonBuilder(lastResult, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                        .setFadeoutTime(100000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(mEditor), Balloon.Position.below);
            }
            return;
        }

        //post同步获取数据
        String result = YouDaoRequest.youDaoRequest(selectedText);
        System.out.println("请求结果为 "+result);
        YouDaoResult youDaoResult = JSON.parseObject(result, YouDaoResult.class);
        if (isZhToEn){
            String[] translationArray = stringListToStringArray(youDaoResult.translation);
            zhToEn(translationArray,e,textRange);
            propertiesComponent.setValues(youDaoResult.query,translationArray);
        }else{
            enToZh(selectedText,youDaoResult,mEditor);
        }
    }

    //显示英译中的结果
    public void enToZh(String selectedText,YouDaoResult youDaoResult,Editor mEditor){
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

    //显示中译英的结果，可以选中并替换
    public void zhToEn(String[] results,AnActionEvent event,TextRange textRange){
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        assert editor != null;
        SelectionModel model = editor.getSelectionModel();
        JBList<String> list = new JBList<>();
        list.setListData(results);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);// 允许可间断的多选// 添加选项选中状态被改变的监听器

        JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(list,null).createPopup();

        list.addListSelectionListener(e -> {
            int index = list.getLeadSelectionIndex();//
            ListModel<String> listModel = list.getModel();// 获取选项数据的 ListModel
            // 输出选中的选项
            selectResult = listModel.getElementAt(index);
            System.out.println("选中: " + index + " = " + listModel.getElementAt(index));
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount() == 2){
                    WriteCommandAction.runWriteCommandAction(event.getProject(), () -> editor.getDocument().replaceString(textRange.getStartOffset(),textRange.getEndOffset(),selectResult));
                    model.removeSelection();//移除选中项
                    popup.cancel();//隐藏列表项
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                int index = list.locationToIndex(e.getPoint());
                list.setSelectedIndex(index);
            }
        });
        list.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {//回车监听事件还是没法响应
                if(e.getKeyCode() == KeyEvent.VK_CONTROL){
                    WriteCommandAction.runWriteCommandAction(event.getProject(), () -> editor.getDocument().replaceString(textRange.getStartOffset(),textRange.getEndOffset(),selectResult));
                    model.removeSelection();//移除选中项
                    popup.cancel();//隐藏列表项
                }
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        list.setSelectedIndex(0);// 设置默认选中项
        popup.showInBestPositionFor(editor);
    }
}
