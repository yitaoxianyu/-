package com.nageoffer.shortlink.admin.remote;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dto.req.RecoverShortLinkReqDTO;
import com.nageoffer.shortlink.admin.remote.req.*;
import com.nageoffer.shortlink.admin.remote.resp.*;
import com.nageoffer.shortlink.admin.remote.resp.stats.ShortLinkStatsRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ShortLinkService {

    default Result<IPage<ShortLinkPageRespDTO>> pageQueryShortLink(ShortLinkPageReqDTO requestParams){
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

    /**
     *
     * @param requestParams gid 集合
     * @return
     */
    default Result<List<ShortLinkQueryCountRespDTO>> queryShortLinkCount(List<String> requestParams) {
        Map<String, Object> map = new HashMap<>();
        map.put("requestParams",requestParams);

        String jsonStr = HttpUtil.get("http://localhost:8002/api/short-link/v1/count", map);
        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

    default Result<Void> updateShortLink(ShortLinkUpdateReqDTO requestParms) {
        String jsonStr = HttpUtil.post(
                "http://localhost:8002/api/short-link/v1/update",
                JSONObject.toJSONString(requestParms)
        );
        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

    default Result<String> getTitleByUrl(String originUrl){
        Map<String,Object> map = new HashMap<>();
        map.put("url",originUrl);

        String jsonStr = HttpUtil.get("http://localhost:8002/api/short-link/v1/url/title",map);
        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

    default Result<String> getFaviconByUrl(String originUrl){
        Map<String,Object> map = new HashMap<>();
        map.put("url",originUrl);

        String jsonStr = HttpUtil.get("http://localhost:8002/api/short-link/v1/url/favicon",map);
        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

    default Result<IPage<RecycleBinPageRespDTO>> pageQueryShortLink(RecycleBinPageReqDTO requestParams){
        HashMap<String,Object> map = new HashMap<>();
        map.put("gids",requestParams.getGids());
        map.put("current",requestParams.getCurrent());
        map.put("size",requestParams.getSize());


        String jsonStr = HttpUtil.get("http://localhost:8002/api/short-link/v1/recycle-bin/page", map);
        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

    default Result<Void> saveShortLink2RecycleBin(SaveShortLinkReqDTO requestParams){
        String jsonStr = HttpUtil.post(
                "http://localhost:8002/api/short-link/v1/recycle-bin/save",
                JSONObject.toJSONString(requestParams)
        );

        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

    default Result<Void> recoverShortLink(RecoverShortLinkReqDTO requestParams){
        String jsonStr = HttpUtil.post(
                "http://localhost:8002/api/short-link/v1/recycle-bin/recover",
                JSONObject.toJSONString(requestParams)
        );

        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

    default Result<Void> removeShortLink(RemoveShortLinkReqDTO requestParams){
        String jsonStr = HttpUtil.post(
                "http://localhost:8002/api/short-link/v1/recycle-bin/remove",
                JSONObject.toJSONString(requestParams)
        );

        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

    default Result<ShortLinkStatsRespDTO> showOneUrlStats(ShortLinkStatsReqDTO requestParams){
        String jsonStr = HttpUtil.get("http://localhost:8002/api/short-link/v1/stats",
                BeanUtil.beanToMap(requestParams)
        );

        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }

    default Result<IPage<ShortLinkAccessLogsRespDTO>> showOneUrlLogs(ShortLinkAccessLogsReqDTO requestParams){
        HashMap<String,Object> map = new HashMap<>();
        map.put("gid",requestParams.getGid());
        map.put("fullShortUrl",requestParams.getFullShortUrl());
        map.put("startDate",requestParams.getStartDate());
        map.put("endDate",requestParams.getEndDate());
        map.put("current",requestParams.getCurrent());
        map.put("size",requestParams.getSize());
        String jsonStr = HttpUtil.get("http://localhost:8002/api/short-link/v1/stats/access-logs",
                map
        );

        return JSONObject.parseObject(jsonStr, new TypeReference<>() {});
    }
}
