package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.SystemConfigCreateDTO;
import com.xx.xianqijava.entity.SystemConfig;
import com.xx.xianqijava.vo.SystemConfigVO;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService extends IService<SystemConfig> {

    /**
     * 创建系统配置
     *
     * @param createDTO 配置信息
     * @return 配置VO
     */
    SystemConfigVO createConfig(SystemConfigCreateDTO createDTO);

    /**
     * 更新系统配置
     *
     * @param configId  配置ID
     * @param createDTO 配置信息
     * @return 配置VO
     */
    SystemConfigVO updateConfig(Long configId, SystemConfigCreateDTO createDTO);

    /**
     * 删除系统配置
     *
     * @param configId 配置ID
     */
    void deleteConfig(Long configId);

    /**
     * 获取配置列表（分页）
     *
     * @param page      分页参数
     * @param groupName 分组名称（可选）
     * @return 配置列表
     */
    IPage<SystemConfigVO> getConfigList(Page<SystemConfig> page, String groupName);

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键获取配置值（带默认值）
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 获取公开配置（Map形式）
     *
     * @return 配置Map
     */
    Map<String, Object> getPublicConfigs();

    /**
     * 根据分组获取配置
     *
     * @param groupName 分组名称
     * @return 配置Map
     */
    Map<String, String> getConfigsByGroup(String groupName);

    /**
     * 获取所有配置列表
     *
     * @return 配置列表
     */
    List<SystemConfigVO> getAllConfigs();

    /**
     * 批量更新配置
     *
     * @param configs 配置列表
     */
    void batchUpdateConfigs(List<SystemConfigCreateDTO> configs);

    /**
     * 按配置键更新配置值
     *
     * @param configKey   配置键
     * @param configValue 配置值
     */
    void updateConfigValue(String configKey, String configValue);
}
