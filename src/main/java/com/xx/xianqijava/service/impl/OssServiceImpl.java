package com.xx.xianqijava.service.impl;

import com.xx.xianqijava.config.OssConfig;
import com.xx.xianqijava.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 阿里云 OSS 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OssConfig ossConfig;

    @Override
    public boolean isEnabled() {
        return ossConfig.getEnabled() != null && ossConfig.getEnabled();
    }

    @Override
    public String getImageUrl(String filename, Integer width, Integer height) {
        if (!isEnabled()) {
            return null;
        }

        // 构建基础 URL
        String baseUrl = ossConfig.getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        String url = baseUrl + filename;

        // 如果指定了尺寸，添加图片处理参数
        if (width != null || height != null) {
            StringBuilder process = new StringBuilder("?x-oss-process=image/resize");

            if (width != null && height != null) {
                // 固定宽高
                process.append(",m_fixed,w_").append(width).append(",h_").append(height);
            } else if (width != null) {
                // 仅指定宽度，高度按比例
                process.append(",w_").append(width);
            } else if (height != null) {
                // 仅指定高度，宽度按比例
                process.append(",h_").append(height);
            }

            url += process.toString();
        }

        return url;
    }

    @Override
    public String getThumbnailUrl(String filename) {
        return getImageUrl(filename, 200, 200);
    }

    @Override
    public String getMediumUrl(String filename) {
        return getImageUrl(filename, 800, 800);
    }

    @Override
    public String getOriginalUrl(String filename) {
        if (!isEnabled()) {
            return null;
        }

        String baseUrl = ossConfig.getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        return baseUrl + filename;
    }
}
