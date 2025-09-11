package com.xyz.control;

import com.xyz.common.ResponseResult;
import com.xyz.service.AppUserService;
import com.xyz.service.RatingService;
import com.xyz.utils.UserAuth;
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

    // orderId和planCode决定了你要评价的东西 如果有多个套餐就要传两次 orderId不变，套餐planCode变就行
    public record RatingReq(Long orderId, int score, String comment){}

    @PostMapping()
    public ResponseResult raing(@RequestBody RatingReq ratingReq){
        Long l = appUserService.usernameToUserId(UserAuth.getCurrentUsername());
        ratingService.submitRatings(ratingReq.orderId(),l,ratingReq.score(), ratingReq.comment());
        return ResponseResult.success();
    }
}
