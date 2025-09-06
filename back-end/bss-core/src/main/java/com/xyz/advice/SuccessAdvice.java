package com.xyz.advice;

/**
 * <p>Package Name: com.xyz.advice </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/6 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class SuccessAdvice {
    public static String insertSuccessMessage(int nums){
        return "已成功记录" + nums + "条信息";
    }

    public static String updateSuccessMessage(int nums){
        return "已成功修改" + nums + "条信息";
    }

    public static String deleteSuccessMessage(int nums){
        return "已成功删除" + nums + "条信息";
    }
}
