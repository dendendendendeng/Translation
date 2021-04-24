package com.mycompany;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.components.JBList;
import com.mycompany.YouDao.YouDaoResult;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.mycompany.Util.CONST_VAL.HISTORY;
import static com.mycompany.Util.Utils.transformResultToHtmlString;

public class NewWordsHistory extends AnAction {
    private LinkedHashMap<String, YouDaoResult> map = HISTORY.getState();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if(map == null) map = new LinkedHashMap<>();

        int size = map.size();
        String[] words = new String[size];
        int index = 0;
        System.out.println("缓存的单词个数为 "+size);
        for(Map.Entry<String,YouDaoResult> entry: map.entrySet()){
            words[index] = entry.getKey();
            System.out.println("缓存的单词为"+words[index]);
            index++;
        }
        JFrame frame = new JFrame();
        frame.setBounds(300,300,300,300);
        JBList<String> list = new JBList<>();
        list.setListData(words);
        list.setSelectedIndex(0);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount() == 2){
                    int index = list.locationToIndex(e.getPoint());
                    JFrame jFrame = new JFrame();
                    jFrame.setBounds(350,350,300,300);
                    JLabel label = new JLabel();
                    String text = transformResultToHtmlString(map.get(words[index]));
                    System.out.println("要显示的文本为 \n"+text);
                    label.setText(text);
                    jFrame.add(label);
                    jFrame.setVisible(true);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                int index = list.locationToIndex(e.getPoint());
                list.setSelectedIndex(index);
            }
        });
        frame.add(list);
        frame.setVisible(true);
    }
}
