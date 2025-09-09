package com.xyz.control;

import com.xyz.common.ResponseResult;
import com.xyz.dto.UserDTO;
import com.xyz.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>Package Name: com.xyz.control </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/9 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @Autowired
    AppUserService userService;

    @PostMapping
    public ResponseResult create(@Valid @RequestBody UserDTO.UpsertUserDTO dto) {
        int i = userService.createUser(dto);
        return ResponseResult.success("created: " + i);
    }

    @PutMapping
    public ResponseResult update(@Valid @RequestBody UserDTO.UpsertUserDTO dto) {
        int i = userService.updateUser(dto);
        return ResponseResult.success("updated: " + i);
    }

    @DeleteMapping("/{username}")
    public ResponseResult delete(@PathVariable String username) {
        int i = userService.deleteUser(username);
        return ResponseResult.success("deleted: " + i);
    }

    @PostMapping("/reset-password")
    public ResponseResult resetPwd(@Valid @RequestBody UserDTO.ResetPasswordDTO dto) {
        int i = userService.resetPassword(dto.username(), dto.newPassword());
        return ResponseResult.success("password-reset: " + i);
    }
}
