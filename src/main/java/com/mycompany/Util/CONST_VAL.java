package com.mycompany.Util;

import com.intellij.openapi.components.ServiceManager;
import com.mycompany.MapOfHistory;

public class CONST_VAL {
    public final static String YOUDAO_APP_URL = "https://openapi.youdao.com/api";
    public final static String YOUDAO_APP_KEY = "1240577b316128ef";
    public final static String YOUDAO_API_CODE = "ljYGzEQQoJgfzQyEr2cGMB5jNPicDuyO";

    public final static MapOfHistory HISTORY = ServiceManager.getService(MapOfHistory.class);//之前保存的数据
}
