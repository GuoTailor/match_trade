package com.mt.mtuser.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

/**
 * Created by gyh on 2020/3/6.
 * @apiDefine User
 * @apiParam {Integer} id 用户id
 * @apiParam {String} phone 电话
 * @apiParam {String} nickName 用户名
 * @apiParam {String} idNum 身份证号码
 * @apiParam {String} password 密码
 * @apiParam {String} userPhoto 头像url地址
 * @apiParam {List} roles 角色信息
 * @apiParam {Date} createTime 注册日期
 * @apiParam {Date} lastTime 注册日期
 * @apiParam {Date} updateTime 最后修改日期
 */
@Table("mt_user")
class User : BaseUser() {
    /**
     * 自增id
     */
    @Id
    override var id: Int? = null

    /**
     * 手机号
     */
    var phone: String? = null

    /**
     * 昵称
     */
    var nickName: String? = null

    /**
     * 身份证号码
     */
    var idNum: String? = null

    /**
     * 密码
     */
    private var password: String? = null

    /**
     * 头像
     */
    var userPhoto: String? = null

    /**
     * 注册时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    var createTime: Date? = null

    /**
     * 最后一次登录时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    var lastTime: Date? = null

    /**
     * 最后修改时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    var updateTime: Date? = null

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    override fun getPassword(): String? {
        return password
    }

    @JsonIgnore
    override fun getUsername(): String {
        return phone!!
    }

    override fun setPassword(password: String?) {
        this.password = password
    }

}