package com.nageoffer.shortlink.project.service;

import java.io.IOException;

public interface UrlService {
    String getTitleByUrl(String originUrl) throws IOException;

    String getFaviconByUrl(String originUrl);
}
