package com.xyz.control;

import com.xyz.common.PageResult;
import com.xyz.common.ResponseResult;
import com.xyz.service.AppUserService;
import com.xyz.service.TicketFlowService;
import com.xyz.service.TicketService;
import com.xyz.ticket.Ticket;
import com.xyz.vo.TicketPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    public record TicketReq(Long ticketId, String note,String username){}
    @PostMapping("/arrived")
    public ResponseResult ticketArrived(@RequestBody TicketReq ticketReq){
        Long id = appUserService.usernameToUserId(ticketReq.username());
        ticketFlowService.markArrived(ticketReq.ticketId(), ticketReq.note(), id);
        return ResponseResult.success();
    }


    @PostMapping("/completed")
    public ResponseResult ticketCompleted(@RequestBody TicketReq ticketReq){
        Long id = appUserService.usernameToUserId(ticketReq.username());
        ticketFlowService.markCompleted(ticketReq.ticketId(), ticketReq.note, id);
        return ResponseResult.success();
    }

    @PostMapping("/cancled")
    public ResponseResult ticketCanceled(@RequestBody TicketReq ticketReq){
        Long id = appUserService.usernameToUserId(ticketReq.username());
        ticketFlowService.cancelTicket(ticketReq.ticketId(), ticketReq.note, id);
        return ResponseResult.success();
    }

    @GetMapping("/page")
    public PageResult<TicketPageVO> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String engineer,   // 用户名/姓名关键字
            @RequestParam(required = false) String addressKw   // 安装地址关键字
    ) {
        return PageResult.of(ticketService.pageTicketVO(current, size, status, engineer, addressKw));
    }
}
