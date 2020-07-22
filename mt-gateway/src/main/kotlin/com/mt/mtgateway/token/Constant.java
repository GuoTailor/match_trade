package com.mt.mtgateway.token;

/**
 * 经常用到的一些通用常量，基本已弃用
 */
public class Constant {
    public static final int JWT_ERRCODE_EXPIRE = 4001;          //Token过期
    public static final int JWT_ERRCODE_FAIL = 4002;            //验证不通过

    /**
     * jwt
     */
    public static final String JWT_ID = "5236A";                                        //jwtid
    public static final String JWT_SECRET = "7786df7fc3a34e26a61c034d5ec8245d";            //密匙
                                     //60秒      分    小时
    public static final long JWT_TTL = 60_000 * 60 * 24 * 7;         //超时

}
