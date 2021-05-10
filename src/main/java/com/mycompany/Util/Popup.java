package com.mycompany.Util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.mycompany.YouDao.YouDaoResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.mycompany.Util.Utils.transformResultToString;

public class Popup {
    private static String selectResult = null;

    public static void popupList(String[] results, AnActionEvent event, TextRange textRange){
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        assert editor != null;
        SelectionModel model = editor.getSelectionModel();
        JBList<String> list = new JBList<>();
        list.setListData(results);

        JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(list,null)
                .createPopup();

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

    public static void popup(YouDaoResult youDaoResult, Editor mEditor){
        String result = transformResultToString(youDaoResult);
        JBPopupFactory factory = JBPopupFactory.getInstance();
        factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(186, 238, 186),
                new Color(73, 117, 73)), null)
                .setFadeoutTime(100000)
                .createBalloon()
                .show(factory.guessBestPopupLocation(mEditor), Balloon.Position.below);
    }
}
