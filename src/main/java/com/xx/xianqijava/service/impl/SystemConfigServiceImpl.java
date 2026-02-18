package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.SystemConfigCreateDTO;
import com.xx.xianqijava.entity.SystemConfig;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.SystemConfigMapper;
import com.xx.xianqijava.service.SystemConfigService;
import com.xx.xianqijava.vo.SystemConfigVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
        implements SystemConfigService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "systemConfig", allEntries = true)
    public SystemConfigVO createConfig(SystemConfigCreateDTO createDTO) {
        log.info("创建系统配置, configKey={}", createDTO.getConfigKey());

        // 检查配置键是否已存在
        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, createDTO.getConfigKey());
        if (count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "配置键已存在");
        }

        SystemConfig config = new SystemConfig();
        BeanUtil.copyProperties(createDTO, config);
        if (config.getSortOrder() == null) {
            config.setSortOrder(0);
        }
        if (config.getIsSystem() == null) {
            config.setIsSystem(0);
        }

        save(config);
        log.info("系统配置创建成功, configId={}", config.getConfigId());

        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "systemConfig", allEntries = true)
    public SystemConfigVO updateConfig(Long configId, SystemConfigCreateDTO createDTO) {
        log.info("更新系统配置, configId={}", configId);

        SystemConfig config = getById(configId);
        if (config == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "配置不存在");
        }

        // 系统配置不允许修改配置键和分组
        if (config.getIsSystem() == 1 && !config.getConfigKey().equals(createDTO.getConfigKey())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统配置不允许修改配置键");
        }

        // 如果修改了配置键，检查新配置键是否已存在
        if (!config.getConfigKey().equals(createDTO.getConfigKey())) {
            LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemConfig::getConfigKey, createDTO.getConfigKey());
            if (count(queryWrapper) > 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "配置键已存在");
            }
        }

        BeanUtil.copyProperties(createDTO, config);
        updateById(config);

        log.info("系统配置更新成功, configId={}", configId);
        return convertToVO(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "systemConfig", allEntries = true)
    public void deleteConfig(Long configId) {
        log.info("删除系统配置, configId={}", configId);

        SystemConfig config = getById(configId);
        if (config == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "配置不存在");
        }

        // 系统配置不允许删除
        if (config.getIsSystem() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统配置不允许删除");
        }

        removeById(configId);
        log.info("系统配置删除成功, configId={}", configId);
    }

    @Override
    public IPage<SystemConfigVO> getConfigList(Page<SystemConfig> page, String groupName) {
        log.info("查询系统配置列表, page={}, groupName={}", page.getCurrent(), groupName);

        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(groupName)) {
            queryWrapper.eq(SystemConfig::getGroupName, groupName);
        }
        queryWrapper.orderByAsc(SystemConfig::getGroupName)
                .orderByAsc(SystemConfig::getSortOrder)
                .orderByDesc(SystemConfig::getCreateTime);

        IPage<SystemConfig> configPage = page(page, queryWrapper);
        return configPage.convert(this::convertToVO);
    }

    @Override
    @Cacheable(value = "systemConfig", key = "#configKey")
    public String getConfigValue(String configKey) {
        log.info("获取系统配置值, configKey={}", configKey);

        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = getOne(queryWrapper);

        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return StrUtil.isNotBlank(value) ? value : defaultValue;
    }

    @Override
    @Cacheable(value = "systemConfig", key = "'public'")
    public Map<String, Object> getPublicConfigs() {
        log.info("获取公开配置列表");

        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getIsPublic, 1);
        List<SystemConfig> configs = list(queryWrapper);

        Map<String, Object> result = new HashMap<>();
        for (SystemConfig config : configs) {
            Object value = parseConfigValue(config.getConfigValue(), config.getConfigType());
            result.put(config.getConfigKey(), value);
        }

        return result;
    }

    @Override
    public Map<String, String> getConfigsByGroup(String groupName) {
        log.info("获取分组配置, groupName={}", groupName);

        LambdaQueryWrapper<SystemConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfig::getGroupName, groupName);
        List<SystemConfig> configs = list(queryWrapper);

        return configs.stream()
                .collect(Collectors.toMap(
                        SystemConfig::getConfigKey,
                        SystemConfig::getConfigValue,
                        (v1, v2) -> v1
                ));
    }

    /**
     * 转换为VO
     */
    private SystemConfigVO convertToVO(SystemConfig config) {
        SystemConfigVO vo = new SystemConfigVO();
        BeanUtil.copyProperties(config, vo);

        if (config.getCreateTime() != null) {
            vo.setCreateTime(config.getCreateTime().toString());
        }
        if (config.getUpdateTime() != null) {
            vo.setUpdateTime(config.getUpdateTime().toString());
        }

        return vo;
    }

    /**
     * 根据类型解析配置值
     */
    private Object parseConfigValue(String value, String type) {
        if (StrUtil.isBlank(value)) {
            return null;
        }

        switch (type) {
            case "number":
                try {
                    if (value.contains(".")) {
                        return Double.parseDouble(value);
                    }
                    return Long.parseLong(value);
                } catch (NumberFormatException e) {
                    return value;
                }
            case "boolean":
                return "true".equalsIgnoreCase(value) || "1".equals(value);
            case "json":
                return JSONUtil.parse(value);
            case "string":
            default:
                return value;
        }
    }
}
