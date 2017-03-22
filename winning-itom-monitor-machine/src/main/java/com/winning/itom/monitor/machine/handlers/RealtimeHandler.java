package com.winning.itom.monitor.machine.handlers;

import com.alibaba.fastjson.JSON;
import com.winning.itom.monitor.api.ICollectDataAnalyzer;
import com.winning.itom.monitor.api.constants.RequestParamConstants;
import com.winning.itom.monitor.api.entity.CollectDataMap;
import com.winning.itom.monitor.api.entity.RequestInfo;
import com.winning.itom.monitor.machine.authorize.AuthorizeException;
import com.winning.itom.monitor.machine.authorize.IAuthorizeService;
import com.winning.transport.core.context.RemoteCallContext;
import com.winning.transport.core.context.RpcConext;
import com.winning.transport.core.handler.DefaultRemoteCallHandler;
import com.winning.transport.core.service.IRemoteService;
import com.winning.transport.core.service.entity.HandleMessageResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by nicholasyan on 17/3/15.
 */
public class RealtimeHandler extends DefaultRemoteCallHandler {

    private final List<ICollectDataAnalyzer> collectDataAnalyzers;
    private final HashMap<String, ICollectDataAnalyzer> mapCollectDataAnalyzer;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private IAuthorizeService authorizeService;

    public RealtimeHandler(String tranCode, IRemoteService remoteService,
                           List<ICollectDataAnalyzer> collectDataAnalyzers) {
        super(tranCode, remoteService);
        this.collectDataAnalyzers = collectDataAnalyzers;
        this.mapCollectDataAnalyzer = new HashMap<>();

        for (ICollectDataAnalyzer analyzer : this.collectDataAnalyzers) {
            this.mapCollectDataAnalyzer.put(analyzer.getCollectDataName(), analyzer);
        }
    }


    @Override
    public HandleMessageResult handleMessage(RemoteCallContext remoteCallContext, String msg) {

        LinkedHashMap<String, Object> hashMap = JSON.parseObject(msg, LinkedHashMap.class);
        CollectDataMap collectDataMap = new CollectDataMap(hashMap);

        if (!hashMap.containsKey(RequestParamConstants.LICENSEKEY))
            return null;

        String licenseKey = hashMap.get(RequestParamConstants.LICENSEKEY).toString();

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setLicenseKey(licenseKey);

        if (hashMap.containsKey(RequestParamConstants.TIMESTAMP)) {
            String timestamp = hashMap.get(RequestParamConstants.TIMESTAMP).toString();
            try {
                requestInfo.setTimestamp(sdf.parse(timestamp).getTime());
            } catch (ParseException e) {
                requestInfo.setTimestamp(new Date().getTime());
            }
        } else {
            requestInfo.setTimestamp(new Date().getTime());
        }

        if (hashMap.containsKey(RequestParamConstants.CLIENT_ID)) {
            requestInfo.setClientId(hashMap.get(RequestParamConstants.CLIENT_ID).toString());
        } else {
            requestInfo.setClientId("ITOM.Client.Test");
        }

        if (hashMap.containsKey(RequestParamConstants.CLIENT_NAME)) {
            requestInfo.setClientName(hashMap.get(RequestParamConstants.CLIENT_NAME).toString());
        } else {
            requestInfo.setClientName("测试客户");
        }

        if (hashMap.containsKey(RequestParamConstants.HOST_IP)) {
            requestInfo.setIpAddress(hashMap.get(RequestParamConstants.HOST_IP).toString());
        } else {
            String remoteIp = RpcConext.getRemoteIp();
            requestInfo.setIpAddress(remoteIp);
        }

        if (hashMap.containsKey(RequestParamConstants.HOST_NAME)) {
            requestInfo.setIpAddress(hashMap.get(RequestParamConstants.HOST_NAME).toString());
        } else {
            String remoteIp = RpcConext.getRemoteIp();
            requestInfo.setIpAddress(remoteIp);
        }

        try {
            if (authorizeService != null)
                authorizeService.checkAuthorize(licenseKey, requestInfo);
        } catch (AuthorizeException e) {
            return null;
        }


        for (String key : hashMap.keySet()) {
            if (this.mapCollectDataAnalyzer.containsKey(key))
                this.mapCollectDataAnalyzer.get(key).analyzeCollectData(requestInfo, collectDataMap);
        }

        return null;
    }


    public IAuthorizeService getAuthorizeService() {
        return authorizeService;
    }

    public void setAuthorizeService(IAuthorizeService authorizeService) {
        this.authorizeService = authorizeService;
    }
}
