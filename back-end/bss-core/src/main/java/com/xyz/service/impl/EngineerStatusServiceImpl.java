package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.user.EngineerStatus;
import com.xyz.service.EngineerStatusService;
import com.xyz.mapper.EngineerStatusMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【ENGINEER_STATUS】的数据库操作Service实现
* @createDate 2025-09-08 20:36:33
*/
@Service
public class EngineerStatusServiceImpl extends ServiceImpl<EngineerStatusMapper, EngineerStatus>
    implements EngineerStatusService{

}




