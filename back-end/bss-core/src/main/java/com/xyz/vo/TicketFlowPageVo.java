package com.xyz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Date;
import java.util.List;

/**
 * <p>Package Name: com.xyz.vo </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/11 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Builder
public record TicketFlowPageVo(
        // TICKET 基本信息
        @Schema(description = "工单ID")
        Long id,

        @Schema(description = "订单ID")
        Long orderId,

        @Schema(description = "工单状态：新建/已分配/进行中/已完成/已取消")
        String status,

        @Schema(description = "指派工程师ID")
        Long assigneeId,

        @Schema(description = "备注")
        String note,

        @Schema(description = "指派时间")
        Date dispatchedAt,

        @Schema(description = "完成时间")
        Date completedAt,

        @Schema(description = "创建时间")
        Date createdAt,

        @Schema(description = "更新时间")
        Date updatedAt,

        // 扩展：Orders
        @Schema(description = "安装地址")
        String installAddress,

        // 扩展：工程师信息（app_user）
        @Schema(description = "工程师用户名")
        String engineerUsername,

        @Schema(description = "工程师姓名")
        String engineerFullName,

        @Schema(description = "工程师电话")
        String engineerPhone,

        @Schema(description = "工程师邮箱")
        String engineerEmail,

        List<TicketEventDetailFlowVO> steps

) {
        public static TicketFlowPageVo toFlowPageVo(TicketPageVO ticketPageVO,List<TicketEventDetailFlowVO> steps){
                return TicketFlowPageVo.builder()
                        .assigneeId(ticketPageVO.assigneeId())
                        .id(ticketPageVO.id())
                        .note(ticketPageVO.note())
                        .completedAt(ticketPageVO.completedAt())
                        .createdAt(ticketPageVO.createdAt())
                        .engineerEmail(ticketPageVO.engineerEmail())
                        .engineerPhone(ticketPageVO.engineerPhone())
                        .engineerFullName(ticketPageVO.engineerFullName())
                        .dispatchedAt(ticketPageVO.dispatchedAt())
                        .orderId(ticketPageVO.orderId())
                        .engineerUsername(ticketPageVO.engineerUsername())
                        .installAddress(ticketPageVO.installAddress())
                        .updatedAt(ticketPageVO.updatedAt())
                        .status(ticketPageVO.status())
                        .steps(steps)
                        .build();
        }
}
