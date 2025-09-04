package com.xyz.mapper;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.xyz.resources.IpPool;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author X
* @description 针对表【IP_POOL(IP地址资源池表，用于管理IP地址的分配和使用)】的数据库操作Mapper
* @createDate 2025-09-04 16:15:08
* @Entity com.xyz.resources.IpPool
*/
@Mapper
public interface IpPoolMapper extends BaseMapper<IpPool> {

    /**
     * 查询目标状态和大于设置的带宽的所有ip
     * @param status 目标状态
     * @param avaliableBandwidth 目标带宽
     * @return IpPool 集合
     */
    List<IpPool> selectAllByStatusAndAvaliableBandwidthGreaterThanEqual(@Param("status") String status, @Param("avaliableBandwidth") Integer avaliableBandwidth);

    /**
     * 查询所有符合的目标状态
     * @param status 目标状态
     * @return ipPool集合
     */
    List<IpPool> selectAllByStatus(@Param("status") String status);

    /**
     * 查询目标订单里所涉及的IP
     * @param orderId 目标订单的id
     * @return IpPool集合
     */
    List<IpPool> searchAllByOrderId(@Param("orderId") Long orderId);

    /**
     * 更新可用带宽
     * @param avaliableBandwidth 目标可用带宽
     * @return 更新结果
     */
    int updateAvaliableBandwidth(@Param("avaliableBandwidth") Integer avaliableBandwidth);

    /**
     * 更新最大带宽
     * @param ipBandwidth 最大带宽值
     * @return 更新结果
     */
    int updateIpBandwidth(@Param("ipBandwidth") Integer ipBandwidth);

    /**
     * 更新状态
     * @param status 目标状态
     * @return 更新结果
     */
    int updateStatus(@Param("status") String status);



}




