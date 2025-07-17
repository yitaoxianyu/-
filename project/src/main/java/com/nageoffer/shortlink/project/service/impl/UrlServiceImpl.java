package com.nageoffer.shortlink.project.service.impl;

import com.nageoffer.shortlink.project.service.UrlService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class UrlServiceImpl implements UrlService {

    @Override
    public String getTitleByUrl(String originUrl) throws IOException {
        URL tagetUrl = new URL(originUrl);
        HttpURLConnection connection = (HttpURLConnection) tagetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(originUrl).get();
            return document.title();
        }
        return "Error while fetching title";
    }

    @Override
    public String getFaviconByUrl(String originUrl) {
        try {
            Document doc = Jsoup.connect(originUrl).get();
            // 查找<link>标签中rel属性包含"icon"的元素
            for (Element link : doc.select("link[rel~=(?i)icon]")) {
                String faviconUrl = link.attr("href");
                // 处理相对路径
                if (!faviconUrl.startsWith("http")) {
                    faviconUrl = originUrl + faviconUrl; // 这里可以根据需要调整
                }
                return faviconUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "未找到favicon";
    }
}
