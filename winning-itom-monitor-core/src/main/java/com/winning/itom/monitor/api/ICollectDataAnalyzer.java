package com.winning.itom.monitor.api;

import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;

/**
 * Created by nicholasyan on 17/3/15.
 */
public interface ICollectDataAnalyzer {

    String getCollectDataName();

    void analyzeCollectData(RequestInfo requestInfo, CollectDataMap collectDataMap);

}
