package com.nageoffer.shortlink.admin.remote;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.resp.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.Map;

public interface ShortLinkService {

    default Result<IPage<ShortLinkPageRespDTO>> PageQueryShortLink(ShortLinkPageReqDTO requestParams){
        Map<String,Object> map = new HashMap<>();
        map.put("gid",requestParams.getGid());
        map.put("current",requestParams.getCurrent());
        map.put("size",requestParams.getSize());

        String jsonStr = HttpUtil.get("http://localhost:8002/api/short-link/v1/page", map);
        return JSONObject.parseObject(jsonStr, new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>() {});
    }

    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParams){
        String jsonStr = HttpUtil.post(
                "http://localhost:8002/api/short-link/v1/create",
                JSONObject.toJSONString(requestParams)
        );
        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

}
