package com.xyz.mapper;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyz.orders.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author X
* @description 针对表【ORDER_ITEM(订单明细表，存储订单中的具体套餐信息)】的数据库操作Mapper
* @createDate 2025-09-07 17:53:24
* @Entity com.xyz.orders.OrderItem
*/
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    IPage<OrderItem> selectPageByUserGroupedOrder(IPage<OrderItem> page,
                                                  @Param("username") String username);

    List<OrderItem> selectAllByUsername(@Param("username") String username);

    int updateOrderIdById(@Param("orderId") Long orderId, @Param("id") Long id);

    int updateStatusById(@Param("status") String status, @Param("id") Long id);

    /**
     * 批量更新订单项的订单ID
     * @param orderId 订单ID
     * @param orderItemIds 订单项ID列表
     * @return 更新记录数
     */
    int batchUpdateOrderIdById(@Param("orderId") Long orderId, @Param("orderItemIds") List<Long> orderItemIds);


    /**
     * 批量更新订单项状态和状态过期时间
     * @param status 新状态
     * @param orderItemIds 订单项ID列表
     * @return 更新记录数
     */
    int batchUpdateStatusAndExpireById(
            @Param("status") String status,
            @Param("orderItemIds") List<Long> orderItemIds);

    /**
     * 根据订单项ID列表查询对应的用户ID列表
     * @param orderItemIds 订单项ID列表
     * @return 用户ID列表
     */
    @Select("<script>" +
            "SELECT username FROM ORDER_ITEM WHERE id IN " +
            "<foreach item='id' collection='orderItemIds' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<String> selectUsernamesByIds(@Param("orderItemIds") List<Long> orderItemIds);

    /**
     * 根据订单项ID列表查询对应的状态列表
     * @param orderItemIds 订单项ID列表
     * @return 状态列表
     */
    List<String> selectStatusByIds(@Param("orderItemIds") List<Long> orderItemIds);


}




