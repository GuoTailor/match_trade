package com.mt.mtuser.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(Util.class);
    private static final Random random = new Random();

    /**
     * 将json格式化为map
     *
     * @param json
     * @return
     */
    public static Map<String, Object> getParameterMap(String json) {
        Map<String, Object> map = new HashMap<>();
        if (!StringUtils.isEmpty(json)) {
            try {
                map = new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

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

    public static String createDate(Date time) {
        return createDate("yyyy-MM-dd HH:mm:ss", time.getTime());
    }

    public static String createDate(String pattern, long time) {
        return new SimpleDateFormat(pattern).format(time);
    }

    /**
     * 把格式化后的时间字符串解码成时间毫秒值
     *
     * @param time 格式化后的时间字符串
     * @return 时间毫秒值
     */
    public static Long encoderDate(String time) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time).getTime();
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

    /**
     * 构建一个把当前{@code roomNumber}加一的房间号<br>
     * 注意可能把当前房间号加一后出现进位，如99加一后为100，长度从两位变成了三位，这是返回一个全0的字符串
     * @param roomNumber 当前的房间号
     * @return 一个新的房间号
     */
    public static String createNewNumber(String roomNumber) {
        if (roomNumber == null) {
            return "0000";
        }
        Matcher matcher = Pattern.compile("[1-9][\\d]*").matcher(roomNumber);
        if (matcher.find()) {
            int start = matcher.start();
            int length = matcher.end() - start;
            String ss = roomNumber.substring(0, start);
            int num = Integer.parseInt(matcher.group()) + 1;
            String es = Integer.toString(num);
            if (es.length() > length) {
                StringBuilder temp = new StringBuilder();
                while (length-- > 0) {
                    temp.append("0");
                }
                es = temp.toString();
            }
            return ss + es;
        } else {
            throw new IllegalStateException("不支持的房间号" + roomNumber);
        }
    }

}
