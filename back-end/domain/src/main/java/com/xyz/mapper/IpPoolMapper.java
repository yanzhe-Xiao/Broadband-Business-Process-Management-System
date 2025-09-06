package com.xyz.mapper;
import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
     * 分页查询目标状态和大于设置的带宽的所有ip
     * @param page 分页对象
     * @param status 目标状态
     * @param avaliableBandwidth 目标带宽
     * @return IpPool 分页结果
     */
    IPage<IpPool> selectAllByStatusAndAvaliableBandwidthGreaterThanEqual(IPage<IpPool> page, @Param("status") String status, @Param("avaliableBandwidth") Integer avaliableBandwidth);

    /**
     * 分页查询所有符合的目标状态
     * @param page 分页对象
     * @param status 目标状态
     * @return ipPool分页结果
     */
    IPage<IpPool> selectAllByStatus(IPage<IpPool> page, @Param("status") String status);

    /**
     * 分页查询目标订单里所涉及的IP
     * @param page 分页对象
     * @param orderId 目标订单的id
     * @return IpPool分页结果
     */
    IPage<IpPool> searchAllByOrderId(IPage<IpPool> page, @Param("orderId") Long orderId);
    /**
     * 通过ip修改最大带宽
     * @param ipBandwidth 最大带宽
     * @param ip ip
     * @return 修改的行数
     */
    int updateIpBandwidthByIp(@Param("ipBandwidth") Integer ipBandwidth, @Param("ip") String ip);

    /**
     * 通过ip修改状态
     * @param status 状态
     * @param ip ip
     * @return 修改的行数
     */
    int updateStatusByIp(@Param("status") String status, @Param("ip") String ip);


    /**
     * 通过ip修改剩余带宽
     * @param avaliableBandwidth 剩余带宽
     * @param ip ip
     * @return 修改的行数
     */
    int updateAvaliableBandwidthByIp(@Param("avaliableBandwidth") Integer avaliableBandwidth, @Param("ip") String ip);

    /**
     * 查找最大剩余的带宽
     * @return 返回最大可用的带宽
     */
    Integer selectMaxAvaliableBandwidth();

}




