package com.xyz.control;

import com.xyz.common.ResponseResult;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Package Name: com.xyz.control </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/3 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/api")
public class TestOrder {


//    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER','ROLE_TECH')")
    @GetMapping("/orders/{i}")
    public int getOrder(@PathVariable int i){
        System.out.println("hello");
        return i;
    }

}
