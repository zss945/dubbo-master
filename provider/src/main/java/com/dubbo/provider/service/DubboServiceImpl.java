package com.dubbo.provider.service;

import com.duboo.api.DubboService;

import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class DubboServiceImpl implements DubboService {

    private Logger logger = LoggerFactory.getLogger(DubboServiceImpl.class);

    @Override
    public Map<String, Object> getServiceInfo() {
        String fullURL = RpcContext.getContext().getUrl().toFullString();
        Map<String, Object> map = new HashMap<>();
        map.put("FullURL", fullURL);
        logger.info("-----fullURL = {}", fullURL);
        return map;
    }

}
