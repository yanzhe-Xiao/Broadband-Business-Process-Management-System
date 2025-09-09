package com.xyz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Date;

@Schema(name = "TicketPageVO", description = "工单分页视图")
@Builder
public record TicketPageVO(

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
        String engineerEmail
) { }
