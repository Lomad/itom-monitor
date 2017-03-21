package com.winning.itom.monitor.api.entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by nicholasyan on 17/3/15.
 */
public class CollectDataMap {

    private final LinkedHashMap<String, CollectData> collectDataLinkedHashMap = new LinkedHashMap<>();

    public CollectDataMap(LinkedHashMap<String, Object> datas) {
        for (Map.Entry<String, Object> entry : datas.entrySet()) {
            CollectData collectData = new CollectData();
            collectData.setName(entry.getKey());
            collectData.setValue(entry.getValue());

            this.collectDataLinkedHashMap.put(collectData.getName(), collectData);
        }
    }


    public CollectData getCollectData(String collectDataName) {
        return this.collectDataLinkedHashMap.get(collectDataName);
    }


}
