package com.xyz.constraints;

import java.util.Calendar;
import java.util.Date;

/**
 * <p>Package Name: com.xyz.constraints </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/5 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class OrderConstarint {
    public static final Double INSTALLATION_FEE = 100.00;

    public static final Date FOREVER_DATE = createFarFutureDate();

    private static Date createFarFutureDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2199, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
