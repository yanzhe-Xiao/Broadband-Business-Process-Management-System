package com.xyz.user;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * <p>Package Name: com.xyz.user </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Data
@TableName("app_user")
public class AppUser {
    @TableId
    private Long id;
    private String username;
    private String password; // BCRYPT
    private String fullName;
    private String email;
    private String phone;
    private String status;   // ACTIVE/LOCKED/...
    private String tenantId; // 可选
    private int deleted;
    private Date daletedAt;
    private int version;
    private Date cretedAt;
    private Date updatedAt;
}
