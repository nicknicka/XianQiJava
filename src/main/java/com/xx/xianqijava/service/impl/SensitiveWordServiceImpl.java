package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.dto.SensitiveWordCheckDTO;
import com.xx.xianqijava.entity.SensitiveWord;
import com.xx.xianqijava.mapper.SensitiveWordMapper;
import com.xx.xianqijava.service.SensitiveWordService;
import com.xx.xianqijava.vo.SensitiveWordCheckVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 敏感词服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements SensitiveWordService {

    @Override
    public SensitiveWordCheckVO checkSensitiveWord(SensitiveWordCheckDTO dto) {
        log.info("检测敏感词, checkType={}, contentLength={}", dto.getCheckType(), dto.getContent().length());

        SensitiveWordCheckVO result = new SensitiveWordCheckVO();
        List<String> foundWords = new ArrayList<>();
        String filteredContent = dto.getContent();

        // 获取所有启用的敏感词
        List<SensitiveWord> sensitiveWords = getActiveSensitiveWords();

        for (SensitiveWord word : sensitiveWords) {
            String keyword = word.getWord();
            if (keyword == null || keyword.isEmpty()) {
                continue;
            }

            // 检查内容中是否包含敏感词
            Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(filteredContent);

            if (matcher.find()) {
                foundWords.add(keyword);

                // 类型1-禁止词：直接拒绝
                if (word.getType() == 1) {
                    result.setHasSensitiveWord(true);
                    result.setPassed(false);
                    result.setSensitiveWords(foundWords);
                    result.setMessage("内容包含禁止词：" + keyword);
                    log.info("检测到禁止词: {}", keyword);
                    return result;
                }

                // 类型3-替换词：替换为指定词
                if (word.getType() == 3 && word.getReplaceWord() != null) {
                    filteredContent = matcher.replaceAll(word.getReplaceWord());
                }
            }
        }

        // 类型2-敏感词：使用*替换
        for (String keyword : foundWords) {
            Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(dto.getContent());
            if (matcher.find()) {
                SensitiveWord word = sensitiveWords.stream()
                        .filter(w -> keyword.equals(w.getWord()))
                        .findFirst()
                        .orElse(null);
                if (word != null && word.getType() == 2) {
                    String replacement = "*".repeat(keyword.length());
                    filteredContent = filteredContent.replaceAll("(?i)" + Pattern.quote(keyword), replacement);
                }
            }
        }

        result.setHasSensitiveWord(!foundWords.isEmpty());
        result.setPassed(true);
        result.setSensitiveWords(foundWords);
        result.setFilteredContent(filteredContent);
        result.setMessage(!foundWords.isEmpty() ? "内容已自动过滤敏感词" : "内容检测通过");

        log.info("敏感词检测完成, foundWords={}", foundWords);
        return result;
    }

    @Override
    public String filterSensitiveWord(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String filteredContent = content;
        List<SensitiveWord> sensitiveWords = getActiveSensitiveWords();

        for (SensitiveWord word : sensitiveWords) {
            String keyword = word.getWord();
            if (keyword == null || keyword.isEmpty()) {
                continue;
            }

            if (word.getType() == 1 || word.getType() == 2) {
                // 禁止词和敏感词：使用*替换
                Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(filteredContent);
                if (matcher.find()) {
                    String replacement = "*".repeat(keyword.length());
                    filteredContent = matcher.replaceAll(replacement);
                }
            } else if (word.getType() == 3 && word.getReplaceWord() != null) {
                // 替换词：替换为指定词
                Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
                filteredContent = pattern.matcher(filteredContent).replaceAll(word.getReplaceWord());
            }
        }

        return filteredContent;
    }

    @Override
    @Cacheable(value = "sensitiveWords", key = "'active'")
    public List<SensitiveWord> getActiveSensitiveWords() {
        log.info("获取所有启用的敏感词");

        LambdaQueryWrapper<SensitiveWord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SensitiveWord::getStatus, 1)
                .orderByAsc(SensitiveWord::getLevel)
                .orderByDesc(SensitiveWord::getWordId);

        return list(queryWrapper);
    }
}
