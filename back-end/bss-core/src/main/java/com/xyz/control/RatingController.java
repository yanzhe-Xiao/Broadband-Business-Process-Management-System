package com.xyz.control;

import com.xyz.common.ResponseResult;
import com.xyz.service.AppUserService;
import com.xyz.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Package Name: com.xyz.control </p> 
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/9 </p>
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/api/rating")
public class RatingController {
    @Autowired
    RatingService ratingService;
    @Autowired
    AppUserService appUserService;

    public record RatingReq(Long orderItemId,String username, int score, String comment){}

    @PostMapping()
    public ResponseResult raing(@RequestBody RatingReq ratingReq){
        Long l = appUserService.usernameToUserId(ratingReq.username());
        ratingService.submitRating(ratingReq.orderItemId(),l,ratingReq.score(), ratingReq.comment());
        return ResponseResult.success();
    }
}
