package com.mt.mtuser.entity

/**
 * Created by gyh on 2020/6/16
 * @apiDefine Analyst
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
 * @apiParam {Integer} companyCount 公司数
 * @apiParam {Integer} reportCount 报告数
 */
class Analyst(
        /*** 公司数 */
        var companyCount: Int = 0,
        var reportCount: Int = 0
): User()