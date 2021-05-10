package com.mycompany;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.TextRange;
import com.mycompany.YouDao.YouDaoResult;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

import static com.mycompany.Util.CONST_VAL.HISTORY;
import static com.mycompany.Util.Popup.popupList;
import static com.mycompany.Util.Utils.stringListToStringArray;
import static com.mycompany.Util.YouDaoRequest.requestAndPopup;

public class TranslateAndReplace extends AnAction {
    private LinkedHashMap<String, YouDaoResult> map = HISTORY.getState();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
        if (null == mEditor) {
            return;
        }

        //获取选中的文本
        SelectionModel model = mEditor.getSelectionModel();
        TextRange textRange = new TextRange(model.getSelectionStart(),model.getSelectionEnd());
        String selectedText = model.getSelectedText();
        if (TextUtils.isEmpty(selectedText)) {
            return;
        }
        System.out.println(selectedText);

        if(map == null) map = new LinkedHashMap<>();
        if(map.get(selectedText) != null) {//已经查询过一次
            YouDaoResult result = map.get(selectedText);
            String[] translationArray = stringListToStringArray(result.translation);
            popupList(translationArray,e,textRange);
            System.out.println("从缓存中获取数据 待查找的值为"+selectedText);
            System.out.println("缓存中单词的个数为 "+map.size());
            return;
        }

        //post异步获取数据并显示列表
        requestAndPopup(selectedText,e,textRange);
    }
}
