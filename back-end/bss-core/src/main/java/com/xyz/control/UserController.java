package com.xyz.control;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.xyz.advice.SuccessAdvice;
import com.xyz.common.PageResult;
import com.xyz.common.ResponseResult;
import com.xyz.dto.PageReq;
import com.xyz.dto.UserDTO;
import com.xyz.service.AppUserService;
import com.xyz.utils.UserAuth;
import com.xyz.vo.Profile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
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
        return ResponseResult.success(SuccessAdvice.insertSuccessMessage(i));
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

    @GetMapping("/me")
    public ResponseResult<String> testGetUsername(){
        String currentUsername = UserAuth.getCurrentUsername();
        return ResponseResult.success(currentUsername);
    }

    @GetMapping("/all")
    public ResponseResult<PageResult<Profile>> getAllUsers(PageReq pageReq){
        IPage<Profile> profileIPage = userService.queryUserPage(pageReq);
        return ResponseResult.success(PageResult.of(profileIPage));
    }
}
