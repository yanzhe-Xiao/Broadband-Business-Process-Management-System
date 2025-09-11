package com.xyz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

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
public record TicketEventDetailFlowVO(
        @Schema(description = "事件编码") String eventCode,
        @Schema(description = "备注") String note,
        @Schema(description = "发生时间") java.util.Date happenedAt,
        @Schema(description = "图片URL列表") java.util.List<String> imageUrls
) {

}
