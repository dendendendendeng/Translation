package com.mycompany;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.popup.*;
import com.intellij.ui.JBColor;
import com.mycompany.Util.YouDaoRequest;
import com.mycompany.YouDao.YouDaoResult;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.LinkedHashMap;

import static com.mycompany.Util.CONST_VAL.HISTORY;
import static com.mycompany.Util.Utils.transformResultToString;

public class Translate extends AnAction {
    private LinkedHashMap<String, YouDaoResult> map = HISTORY.getState();

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
        if(map == null) map = new LinkedHashMap<>();
        if(map.get(selectedText) != null) {//已经查询过一次
            popup(map.get(selectedText),mEditor);
            System.out.println("从缓存中获取数据 待查找的值为"+selectedText);
            System.out.println("缓存中单词的个数为 "+map.size());
        }

        //post同步获取数据
        String result = YouDaoRequest.youDaoRequest(selectedText);
        System.out.println("请求结果为 "+result);
        YouDaoResult youDaoResult = JSON.parseObject(result, YouDaoResult.class);
        popup(youDaoResult,mEditor);

        map.put(youDaoResult.query,youDaoResult);
        HISTORY.loadState(map);
    }

    //显示英译中的结果
    public void popup( YouDaoResult youDaoResult, Editor mEditor){
        String result = transformResultToString(youDaoResult);
        JBPopupFactory factory = JBPopupFactory.getInstance();
        factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                .setFadeoutTime(100000)
                .createBalloon()
                .show(factory.guessBestPopupLocation(mEditor), Balloon.Position.below);
    }
}
