package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.ProductFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品收藏Mapper接口
 */
@Mapper
public interface ProductFavoriteMapper extends BaseMapper<ProductFavorite> {
}
