package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.SensitiveWordCheckDTO;
import com.xx.xianqijava.entity.SensitiveWord;
import com.xx.xianqijava.vo.SensitiveWordCheckVO;

import java.util.List;

/**
 * 敏感词服务接口
 */
public interface SensitiveWordService extends IService<SensitiveWord> {

    /**
     * 检测敏感词
     *
     * @param dto 检测请求
     * @return 检测结果
     */
    SensitiveWordCheckVO checkSensitiveWord(SensitiveWordCheckDTO dto);

    /**
     * 过滤敏感词（替换为*或替换词）
     *
     * @param content 原始内容
     * @return 过滤后的内容
     */
    String filterSensitiveWord(String content);

    /**
     * 获取所有启用的敏感词
     *
     * @return 敏感词列表
     */
    List<SensitiveWord> getActiveSensitiveWords();
}
