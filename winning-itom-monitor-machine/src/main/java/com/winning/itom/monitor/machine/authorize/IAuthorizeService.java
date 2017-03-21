package com.winning.itom.monitor.machine.authorize;

import com.winning.itom.monitor.api.entity.RequestInfo;

/**
 * Created by nicholasyan on 17/3/21.
 */
public interface IAuthorizeService {

    void checkAuthorize(String licenseKey, RequestInfo requestInfo)
            throws AuthorizeException;

}
