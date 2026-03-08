package com.xx.xianqijava.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.annotation.OperationLog;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.SensitiveWord;
import com.xx.xianqijava.mapper.SensitiveWordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 敏感词管理后台接口
 */
@Slf4j
@Tag(name = "敏感词管理后台")
@RestController
@RequestMapping("/admin/sensitive-word")
@RequiredArgsConstructor
public class SensitiveWordManageController {

    private final SensitiveWordMapper sensitiveWordMapper;

    // ========== 查询接口 ==========

    /**
     * 获取敏感词列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取敏感词列表")
    public Result<IPage<SensitiveWord>> getSensitiveWordList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "敏感词（模糊搜索）") @RequestParam(required = false) String word,
            @Parameter(description = "类型") @RequestParam(required = false) Integer type,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        log.info("查询敏感词列表, page={}, size={}, word={}, type={}, status={}", page, size, word, type, status);

        Page<SensitiveWord> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<SensitiveWord> queryWrapper = new LambdaQueryWrapper<>();

        if (word != null && !word.trim().isEmpty()) {
            queryWrapper.like(SensitiveWord::getWord, word);
        }
        if (type != null) {
            queryWrapper.eq(SensitiveWord::getType, type);
        }
        if (status != null) {
            queryWrapper.eq(SensitiveWord::getStatus, status);
        }

        queryWrapper.orderByDesc(SensitiveWord::getCreateTime);

        IPage<SensitiveWord> resultPage = sensitiveWordMapper.selectPage(pageParam, queryWrapper);

        return Result.success(resultPage);
    }

    /**
     * 获取敏感词详情
     */
    @GetMapping("/{wordId}")
    @Operation(summary = "获取敏感词详情")
    public Result<SensitiveWord> getSensitiveWordDetail(
            @Parameter(description = "敏感词ID") @PathVariable Long wordId) {
        log.info("获取敏感词详情, wordId={}", wordId);

        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(wordId);
        if (sensitiveWord == null) {
            return Result.error("敏感词不存在");
        }

        return Result.success(sensitiveWord);
    }

    /**
     * 获取敏感词统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取敏感词统计")
    public Result<Map<String, Object>> getStatistics() {
        log.info("获取敏感词统计");

        Map<String, Object> stats = new HashMap<>();

        // 总数
        Long total = sensitiveWordMapper.selectCount(new LambdaQueryWrapper<>());
        stats.put("total", total);

        // 按类型统计
        Map<String, Long> typeStats = new HashMap<>();
        typeStats.put("forbidden", sensitiveWordMapper.selectCount(
            new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getType, 1)
        ));
        typeStats.put("sensitive", sensitiveWordMapper.selectCount(
            new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getType, 2)
        ));
        typeStats.put("replace", sensitiveWordMapper.selectCount(
            new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getType, 3)
        ));
        stats.put("typeStats", typeStats);

        // 按状态统计
        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("active", sensitiveWordMapper.selectCount(
            new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getStatus, 1)
        ));
        statusStats.put("inactive", sensitiveWordMapper.selectCount(
            new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getStatus, 0)
        ));
        stats.put("statusStats", statusStats);

        return Result.success(stats);
    }

    // ========== 增删改接口 ==========

    /**
     * 创建敏感词
     */
    @PostMapping
    @Operation(summary = "创建敏感词")
    @OperationLog(
            module = "sensitive_word",
            action = "create",
            description = "创建敏感词"
    )
    public Result<Long> createSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        log.info("创建敏感词: word={}, type={}", sensitiveWord.getWord(), sensitiveWord.getType());

        // 验证必填字段
        if (sensitiveWord.getWord() == null || sensitiveWord.getWord().trim().isEmpty()) {
            return Result.error("敏感词不能为空");
        }
        if (sensitiveWord.getType() == null) {
            return Result.error("类型不能为空");
        }

        // 检查是否已存在
        Long count = sensitiveWordMapper.selectCount(
            new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getWord, sensitiveWord.getWord())
        );
        if (count > 0) {
            return Result.error("该敏感词已存在");
        }

        // 设置默认值
        if (sensitiveWord.getStatus() == null) {
            sensitiveWord.setStatus(1); // 默认启用
        }
        if (sensitiveWord.getLevel() == null) {
            sensitiveWord.setLevel(1); // 默认一般
        }

        sensitiveWord.setCreateTime(LocalDateTime.now());
        sensitiveWord.setUpdateTime(LocalDateTime.now());
        sensitiveWord.setDeleted(0);

        sensitiveWordMapper.insert(sensitiveWord);
        log.info("创建敏感词成功, wordId={}", sensitiveWord.getWordId());

        return Result.success(sensitiveWord.getWordId());
    }

    /**
     * 更新敏感词
     */
    @PutMapping
    @Operation(summary = "更新敏感词")
    @OperationLog(
            module = "sensitive_word",
            action = "update",
            description = "更新敏感词"
    )
    public Result<Boolean> updateSensitiveWord(@RequestBody SensitiveWord sensitiveWord) {
        log.info("更新敏感词: wordId={}", sensitiveWord.getWordId());

        if (sensitiveWord.getWordId() == null) {
            return Result.error("敏感词ID不能为空");
        }

        SensitiveWord existing = sensitiveWordMapper.selectById(sensitiveWord.getWordId());
        if (existing == null) {
            return Result.error("敏感词不存在");
        }

        // 检查敏感词是否重复
        if (sensitiveWord.getWord() != null && !sensitiveWord.getWord().equals(existing.getWord())) {
            Long count = sensitiveWordMapper.selectCount(
                new LambdaQueryWrapper<SensitiveWord>()
                    .eq(SensitiveWord::getWord, sensitiveWord.getWord())
                    .ne(SensitiveWord::getWordId, sensitiveWord.getWordId())
            );
            if (count > 0) {
                return Result.error("该敏感词已存在");
            }
        }

        sensitiveWord.setUpdateTime(LocalDateTime.now());
        sensitiveWordMapper.updateById(sensitiveWord);

        log.info("更新敏感词成功, wordId={}", sensitiveWord.getWordId());
        return Result.success(true);
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/{wordId}")
    @Operation(summary = "删除敏感词")
    @OperationLog(
            module = "sensitive_word",
            action = "delete",
            description = "删除敏感词"
    )
    public Result<Boolean> deleteSensitiveWord(
            @Parameter(description = "敏感词ID") @PathVariable Long wordId) {
        log.info("删除敏感词, wordId={}", wordId);

        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(wordId);
        if (sensitiveWord == null) {
            return Result.error("敏感词不存在");
        }

        sensitiveWordMapper.deleteById(wordId);

        log.info("删除敏感词成功, wordId={}", wordId);
        return Result.success(true);
    }

    /**
     * 批量删除敏感词
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除敏感词")
    @OperationLog(
            module = "sensitive_word",
            action = "batch_delete",
            description = "批量删除敏感词"
    )
    public Result<Boolean> batchDeleteSensitiveWord(@RequestBody List<Long> wordIds) {
        log.info("批量删除敏感词, 数量={}", wordIds.size());

        if (wordIds == null || wordIds.isEmpty()) {
            return Result.error("请选择要删除的敏感词");
        }

        sensitiveWordMapper.deleteBatchIds(wordIds);

        log.info("批量删除敏感词成功, 数量={}", wordIds.size());
        return Result.success(true);
    }

    /**
     * 更新敏感词状态
     */
    @PutMapping("/{wordId}/status")
    @Operation(summary = "更新敏感词状态")
    @OperationLog(
            module = "sensitive_word",
            action = "update_status",
            description = "更新敏感词状态"
    )
    public Result<Boolean> updateStatus(
            @Parameter(description = "敏感词ID") @PathVariable Long wordId,
            @Parameter(description = "状态（0-禁用，1-启用）") @RequestParam Integer status) {
        log.info("更新敏感词状态, wordId={}, status={}", wordId, status);

        SensitiveWord sensitiveWord = sensitiveWordMapper.selectById(wordId);
        if (sensitiveWord == null) {
            return Result.error("敏感词不存在");
        }

        sensitiveWord.setStatus(status);
        sensitiveWord.setUpdateTime(LocalDateTime.now());
        sensitiveWordMapper.updateById(sensitiveWord);

        log.info("更新敏感词状态成功, wordId={}, status={}", wordId, status);
        return Result.success(true);
    }
}
