package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.entity.RefundRecord;
import com.xx.xianqijava.vo.RefundVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 退款记录Mapper接口
 */
@Mapper
public interface RefundRecordMapper extends BaseMapper<RefundRecord> {

    /**
     * 分页查询用户的退款列表（作为买家）
     */
    @Select("""
        SELECT r.*,
               o.order_no,
               o.product_id,
               p.title as product_name,
               pi.image_url as product_image,
               o.buyer_id,
               u1.nickname as buyer_nickname,
               o.seller_id,
               u2.nickname as seller_nickname
        FROM refund_record r
        LEFT JOIN `order` o ON r.order_id = o.order_id
        LEFT JOIN product p ON o.product_id = p.product_id
        LEFT JOIN product_image pi ON p.product_id = pi.product_id AND pi.is_cover = 1
        LEFT JOIN user u1 ON o.buyer_id = u1.user_id
        LEFT JOIN user u2 ON o.seller_id = u2.user_id
        WHERE r.buyer_id = #{userId} AND r.deleted = 0
        ORDER BY r.create_time DESC
        """)
    IPage<RefundVO> selectBuyerRefundPage(Page<RefundVO> page, @Param("userId") Long userId);

    /**
     * 分页查询用户的退款列表（作为卖家）
     */
    @Select("""
        SELECT r.*,
               o.order_no,
               o.product_id,
               p.title as product_name,
               pi.image_url as product_image,
               o.buyer_id,
               u1.nickname as buyer_nickname,
               o.seller_id,
               u2.nickname as seller_nickname
        FROM refund_record r
        LEFT JOIN `order` o ON r.order_id = o.order_id
        LEFT JOIN product p ON o.product_id = p.product_id
        LEFT JOIN product_image pi ON p.product_id = pi.product_id AND pi.is_cover = 1
        LEFT JOIN user u1 ON o.buyer_id = u1.user_id
        LEFT JOIN user u2 ON o.seller_id = u2.user_id
        WHERE r.seller_id = #{userId} AND r.deleted = 0
        ORDER BY r.create_time DESC
        """)
    IPage<RefundVO> selectSellerRefundPage(Page<RefundVO> page, @Param("userId") Long userId);
}
