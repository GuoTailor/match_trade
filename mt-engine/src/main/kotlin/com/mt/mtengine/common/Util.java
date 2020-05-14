package com.mt.mtengine.common;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

public class Util {
    private static final Logger logger = LoggerFactory.getLogger(Util.class);
    private static final Random random = new Random();

    /**
     * 按当前时间，按{@code yyyy-MM-dd HH:mm:ss}格式格式化一个时间字符串
     *
     * @return 格式化后的时间字符串
     */
    public static String createDate() {
        return createDate("yyyy-MM-dd HH:mm:ss");
    }

    public static String createDate(String pattern) {
        return createDate(pattern, System.currentTimeMillis());
    }

    public static String createDate(long time) {
        return createDate("yyyy-MM-dd HH:mm:ss", time);
    }

    public static String createDate(String pattern, long time) {
        return new SimpleDateFormat(pattern).format(time);
    }

    /**
     * 把格式化后的时间字符串解码成时间毫秒值
     * 格式： yyyy-MM-dd HH:mm:ss
     *
     * @param time 格式化后的时间字符串
     * @return 时间毫秒值
     */
    public static Date encoderDate(String time) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断一个（bean）实体类对象是否为空<p>
     * 判断为空的标准为：<P>
     * <ol>
     * <li>如果实体类的属性为{@link String}那么字符串长度为0或为null就认为为空</li>
     * <li>如果属性为{@link Collection}的子类那么集合的长度为0或为null就认为为空</li>
     * <li>如果属性不为上述的就为null才认为为空</li>
     * </ol>
     *
     * @param obj 一个实体类（bean）对象
     * @return true：如果该实体类的所有属性都为空，false：其中的任意一个属性不为空
     */
    public static boolean isEmpty(Object obj) {
        return isEmpty(obj, true);
    }

    /**
     * 判断一个（bean）实体类对象是否为空<p>
     * 非严格模式下判断为空的标准为：
     * 对象的属性是否为null<P>
     * 严格模式下判断为空的标准为：<P>
     * <ol>
     * <li>如果实体类的属性为{@link String}那么字符串长度为0或为null就认为为空</li>
     * <li>如果属性为{@link Collection}的子类那么集合的长度为0或为null就认为为空</li>
     * <li>如果属性不为上述的就为null才认为为空</li>
     * </ol>
     *
     * @param obj    一个实体类（bean）对象
     * @param strict 是否使用严格模式
     * @return true：如果该实体类的所有属性都为空，false：其中的任意一个属性不为空
     */
    public static boolean isEmpty(Object obj, boolean strict) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] proDescriptors = beanInfo == null ? null : beanInfo.getPropertyDescriptors();
            if (proDescriptors != null && proDescriptors.length > 0) {
                for (PropertyDescriptor propDesc : proDescriptors) {
                    Object o = propDesc.getReadMethod().invoke(obj);
                    if (o == null || o.equals(obj.getClass())) {
                        continue;
                    }
                    if (!strict) {
                        return false;
                    }
                    if (o instanceof String) {
                        if (!((String) o).isEmpty()) {
                            return false;
                        } else
                            continue;
                    }
                    if (o instanceof Collection) {
                        if (!((Collection) o).isEmpty())
                            return false;
                        else
                            continue;
                    }
                    return false;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | IntrospectionException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return true;
    }

    /**
     * 随机生成指定长度的数字验证码<br>
     * 考虑使用{@link RandomStringUtils#randomNumeric(int)}
     *
     * @param length 数字验证码长度
     * @return 数字验证码
     */
    public static String getRandomInt(int length) {
        int max = 1;
        for (int i = 0; i < length; i++) {
            max *= 10;
        }
        StringBuilder nextInt = new StringBuilder(String.valueOf(random.nextInt(max)));
        while (nextInt.length() < length) {
            nextInt.insert(0, '0');
        }
        return nextInt.toString();
    }

    public static String addDecimal(String d1, String d2) {
        return new BigDecimal(d1).add(new BigDecimal(d2)).toString();
    }

    public static String subtractDecimal(String d1, String d2) {
        return new BigDecimal(d1).subtract(new BigDecimal(d2)).toString();
    }

    /**
     * 乘
     * @param d1 被乘数
     * @param d2 乘数
     * @return 结果
     */
    public static String multiplyDecimal(String d1, String d2) {
        return new BigDecimal(d1).multiply(new BigDecimal(d2)).toString();
    }

    /**
     * 除,如果除不尽结果使用四舍五入
     * @param d1 被除数
     * @param d2 除数
     * @return 结果
     */
    public static String divideDecimal(String d1, String d2) {
        return new BigDecimal(d1).divide(new BigDecimal(d2), RoundingMode.HALF_UP).toString();
    }

}
