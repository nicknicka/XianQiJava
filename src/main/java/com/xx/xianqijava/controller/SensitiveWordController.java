package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.SensitiveWordCheckDTO;
import com.xx.xianqijava.service.SensitiveWordService;
import com.xx.xianqijava.vo.SensitiveWordCheckVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 敏感词控制器
 */
@Slf4j
@Tag(name = "敏感词管理")
@RestController
@RequestMapping("/sensitive-word")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    /**
     * 检测敏感词
     */
    @PostMapping("/check")
    @Operation(summary = "检测敏感词")
    public Result<SensitiveWordCheckVO> checkSensitiveWord(@Valid @RequestBody SensitiveWordCheckDTO dto) {
        log.info("检测敏感词, checkType={}, contentLength={}", dto.getCheckType(), dto.getContent().length());
        SensitiveWordCheckVO result = sensitiveWordService.checkSensitiveWord(dto);
        return Result.success(result);
    }

    /**
     * 过滤敏感词
     */
    @PostMapping("/filter")
    @Operation(summary = "过滤敏感词")
    public Result<String> filterSensitiveWord(@RequestBody String content) {
        log.info("过滤敏感词, contentLength={}", content.length());
        String filteredContent = sensitiveWordService.filterSensitiveWord(content);
        return Result.success(filteredContent);
    }
}
