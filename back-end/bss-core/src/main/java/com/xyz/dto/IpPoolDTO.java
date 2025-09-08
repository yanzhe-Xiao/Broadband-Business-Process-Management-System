package com.xyz.dto;

import com.xyz.annotation.ValidByPrefix;
import com.xyz.constraints.IpConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * <p>Package Name: com.xyz.dto </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/4 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @sinc
 */
public class IpPoolDTO {
    public record AddIpPool(
            @NotBlank
            @Pattern(
                    regexp = "^(?:(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$",
                    message = "Invalid IP address format"
            )
            String ip,

            @NotBlank
            @ValidByPrefix(prefix = "IP_STATUS_", sources = {IpConstraint.class})
            String status,

            Integer ipBandwidth,
            Integer avaliableBandwidth
    ) {}

}
