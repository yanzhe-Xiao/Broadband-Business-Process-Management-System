package com.xyz.advice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <p>Package Name: com.xyz.common </p>
 * <p>Description: image url 拼接 </p>
 * <p>Create Time: 2025/9/7 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since 21
 */
@Component
public class ImageUrlSplicing {

    private static String serverIp;
    private static String serverPort;

    @Value("${app.server-ip}")
    public void setServerIp(String serverIp) {
        ImageUrlSplicing.serverIp = serverIp;
    }

    @Value("${app.server-port}")
    public void setServerPort(String serverPort) {
        ImageUrlSplicing.serverPort = serverPort;
    }

    public static String splicingURL(String url){
        String fullImageUrl = null;
        if (url != null) {
            fullImageUrl = serverIp + ":" + serverPort + url; // 拼接基地址
        }
        return fullImageUrl;
    }
}
