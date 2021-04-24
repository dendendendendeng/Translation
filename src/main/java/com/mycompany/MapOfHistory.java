package com.mycompany;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.mycompany.YouDao.YouDaoResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

@State(name = "MapOfHistory",
        storages = {
                @Storage(value = "MapOfHistory.xml")
        })
public class MapOfHistory implements PersistentStateComponent<LinkedHashMap<String, YouDaoResult>> {
    private LinkedHashMap<String,YouDaoResult> map = new LinkedHashMap<>();
    @Override
    public @Nullable LinkedHashMap<String, YouDaoResult> getState() {
        return map;
    }

    @Override
    public void loadState(@NotNull LinkedHashMap<String, YouDaoResult> state) {
        map = state;
    }
}
