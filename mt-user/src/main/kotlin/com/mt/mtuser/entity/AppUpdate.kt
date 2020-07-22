package com.mt.mtuser.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

/**
 * @apiDefine AppUpdate
 * @apiParam {Integer} id id
 * @apiParam {String} versionCode 版本号
 * @apiParam {String} versionName 版本名称
 * @apiParam {String} versionInfo 版本信息
 * @apiParam {Boolean} forceUpdate 是否强制更新
 * @apiParam {String} downloadUrlAndroid android版本下载链接
 * @apiParam {String} downloadUrlIos iso下载链接
 * @apiParam {String} downloadUrl 版本下载链接
 * @apiParam {String} time 更新时间
 */
@Table("mt_app_update")
class AppUpdate {
    /** **/
    @Id
    var id: Int? = null

    /*** 版本号*/
    var versionCode: String? = null

    /*** 版本名称*/
    var versionName: String? = null

    /*** 版本信息*/
    var versionInfo: String? = null

    /*** 是否强制更新*/
    var forceUpdate: Boolean? = null

    /*** 版本下载链接*/
    var downloadUrlAndroid: String? = null

    /*** iso下载链接*/
    var downloadUrlIos: String? = null

    /*** 版本下载链接*/
    var downloadUrl: String? = null

    /*** 更新时间*/
    var time: Date? = null

}