package com.xyz.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TicketEventDetailVO", description = "单个工单事件详情")
@lombok.Builder
public record TicketEventDetailVO(
        @Schema(description = "工单ID") Long ticketId,
        @Schema(description = "事件编码") String eventCode,
        @Schema(description = "备注") String note,
        @Schema(description = "发生时间") java.util.Date happenedAt,
        @Schema(description = "图片URL列表") java.util.List<String> imageUrls
) {}
