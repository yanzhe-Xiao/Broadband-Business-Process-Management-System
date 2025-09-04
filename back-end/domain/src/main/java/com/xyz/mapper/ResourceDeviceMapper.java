package com.xyz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyz.resources.ResourceDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * <p>Package Name: com.xyz.mapper </p>
 * <p>Description: 针对表【RESOURCE_DEVICE(网络设备资源表)】的数据库操作Mapper接口。该接口用于管理网络设备的库存和分配状态，并继承自MyBatis-Plus的BaseMapper以获得通用的CRUD能力。</p>
 * <p>Create Time: 2025-09-04 19:11:34 </p>
 *
 * @author X
 * @version 1.0
 * @since
 * @Entity com.xyz.resources.ResourceDevice
 */
@Mapper
public interface ResourceDeviceMapper extends BaseMapper<ResourceDevice> {

    /**
     * 批量插入网络设备资源。
     *
     * @param resourceDeviceCollection 包含待插入的 ResourceDevice 对象的集合。
     * @return 返回成功插入的记录数。
     */
    int insertBatch(@Param("resourceDeviceCollection") Collection<ResourceDevice> resourceDeviceCollection);

    /**
     * 根据状态查询所有符合条件的网络设备。
     *
     * @param status 设备状态（例如：'AVAILABLE', 'IN_USE', 'MAINTENANCE'）。
     * @return 返回符合指定状态的设备列表。
     */
    List<ResourceDevice> selectAllByStatus(@Param("status") String status);

    /**
     * 根据设备型号查询网络设备。
     *
     * @param model 设备型号。
     * @return 返回具有指定型号的设备列表。
     */
    List<ResourceDevice> selectByModel(@Param("model") String model);

    /**
     * 查询价格小于或等于指定值的网络设备。
     *
     * @param price 指定的价格上限。
     * @return 返回价格不高于指定值的设备列表。
     */
    List<ResourceDevice> selectByPriceLessThanEqual(@Param("price") Integer price);

    /**
     * 更新设备的状态。
     * 注意：此方法通常需要与XML中的<update>标签配合使用，并包含一个WHERE子句来指定更新哪些记录。
     *
     * @param status 新的设备状态。
     * @return 返回受影响的行数。
     */
    int updateStatus(@Param("status") String status);

    /**
     * 更新设备的型号。
     * 注意：此方法通常需要与XML中的<update>标签配合使用，并包含一个WHERE子句来指定更新哪些记录。
     *
     * @param model 新的设备型号。
     * @return 返回受影响的行数。
     */
    int updateModel(@Param("model") String model);

    /**
     * 更新设备的价格。
     * 注意：此方法通常需要与XML中的<update>标签配合使用，并包含一个WHERE子句来指定更新哪些记录。
     *
     * @param price 新的设备价格。
     * @return 返回受影响的行数。
     */
    int updatePrice(@Param("price") Integer price);

    /**
     * 更新设备的数量。
     * 注意：此方法通常需要与XML中的<update>标签配合使用，并包含一个WHERE子句来指定更新哪些记录。
     *
     * @param qty 新的设备数量。
     * @return 返回受影响的行数。
     */
    int updateQty(@Param("qty") Integer qty);


}