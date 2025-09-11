package com.xyz.control;

import com.xyz.common.HttpStatusEnum;
import com.xyz.common.PageResult;
import com.xyz.common.ResponseResult;
import com.xyz.constraints.TicketEventCodes;
import com.xyz.service.AppUserService;
import com.xyz.service.TicketEventService;
import com.xyz.service.TicketFlowService;
import com.xyz.service.TicketService;
import com.xyz.utils.UserAuth;
import com.xyz.vo.TicketEventDetailVO;
import com.xyz.vo.TicketFlowPageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <p>Package Name: com.xyz.control </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/8 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/api/ticket")
public class TicketController {
    @Autowired
    TicketFlowService ticketFlowService;
    @Autowired
    AppUserService appUserService;
    @Autowired
    TicketService ticketService;
    @Autowired
    TicketEventService ticketEventService;

    public record TicketReq(Long ticketId, String note, List<String> base64, String eventCode){}
    @PostMapping("/flow")
    public ResponseResult ticketFlow(@RequestBody TicketReq ticketReq){
        Long id = appUserService.usernameToUserId(UserAuth.getCurrentUsername());
        String status = ticketReq.eventCode;
        if (status.equals(TicketEventCodes.SITE_SURVEY.getDescription())) {
            ticketFlowService.markArrived(ticketReq.ticketId(), ticketReq.note(), id, ticketReq.base64());
        } else if (status.equals(TicketEventCodes.CUSTOMER_SIGNATURE.getDescription())) {
            ticketFlowService.markCompleted(ticketReq.ticketId(), ticketReq.note(), id, ticketReq.base64());
        } else if (status.equals(TicketEventCodes.OPTICAL_POWER_TEST.getDescription())
                || status.equals(TicketEventCodes.DEVICE_INSTALLATION.getDescription())
                || status.equals(TicketEventCodes.NETWORK_SPEED_TEST.getDescription())
                || status.equals(TicketEventCodes.WIRING_SPLICING.getDescription())) {
            ticketFlowService.markOtherStatus(ticketReq.ticketId(), ticketReq.note(), id, ticketReq.base64(),
                    Objects.requireNonNull(TicketEventCodes.fromDescription(status)));
        } else {
            return ResponseResult.fail("非可识别状态", HttpStatusEnum.BAD_REQUEST);
        }
        return ResponseResult.success();
    }


    @PostMapping("/cancled")
    public ResponseResult ticketCanceled(@RequestBody TicketReq ticketReq){
        Long id = appUserService.usernameToUserId(UserAuth.getCurrentUsername());
        ticketFlowService.cancelTicket(ticketReq.ticketId(), ticketReq.note, id);
        return ResponseResult.success();
    }

    @GetMapping("/page")
    public ResponseResult<PageResult<TicketFlowPageVo>>  page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String engineer,   // 用户名/姓名关键字
            @RequestParam(required = false) String addressKw   // 安装地址关键字
    ) {
        engineer = UserAuth.getCurrentUsername();
        return ResponseResult.success(PageResult.of(ticketService.pageTicketVO(current, size, status, engineer,
                addressKw)));
    }

    @GetMapping("/flow/get")
    public ResponseResult<TicketEventDetailVO> getTicketEventTimeline(Long ticketId, String status){
        TicketEventDetailVO ticketEventTimelineVO = ticketEventService.queryEventDetail(ticketId, status);
        return ResponseResult.success(ticketEventTimelineVO);
    }

    @GetMapping("/flow/newest")
    public ResponseResult<String> getNewestTicketEventTimeline(Long ticketId){
        String newestStatus = ticketEventService.getByNewestStatus(ticketId);
        return ResponseResult.success(newestStatus);
    }
}
