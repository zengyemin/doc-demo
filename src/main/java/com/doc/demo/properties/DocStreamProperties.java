package com.doc.demo.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author : zengYeMin
 * @date : 2022/3/30 15:26
 **/
@Component
@ConfigurationProperties(prefix = "doc-stream")
public class DocStreamProperties {
    private boolean initMinio = false;
    private int port;
    private String url;
    private String accessKey;
    private String secretKey;
    /**
     * 当前参数作用于本服务器的分段上传
     */
    private Map<String, String> multiPartRootPathMap;

    public boolean isInitMinio() {
        return initMinio;
    }

    public void setInitMinio(boolean initMinio) {
        this.initMinio = initMinio;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Map<String, String> getMultiPartRootPathMap() {
        return multiPartRootPathMap;
    }

    public String getMultiPartTempPath() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("win")) {
            return multiPartRootPathMap.get("Windows");
        } else {
            return multiPartRootPathMap.get("Linux");
        }
    }

    public void setMultiPartRootPathMap(Map<String, String> multiPartRootPathMap) {
        this.multiPartRootPathMap = multiPartRootPathMap;
    }
}
