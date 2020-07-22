package com.mt.mtuser.controller

import com.mt.mtuser.entity.Notify
import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.service.NotifyService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/5/27.
 */
@RestController
@RequestMapping("/notify")
class NotifyController {
    @Autowired
    private lateinit var notifyService: NotifyService

    /**
     * @api {get} /notify/count 获取未读消息的计数
     * @apiDescription  获取未读消息的计数
     * @apiName getUnreadCount
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": 2}
     * @apiGroup Notify
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping("/count")
    fun getUnreadCount(): Mono<ResponseInfo<Long>> {
        return ResponseInfo.ok(mono { notifyService.getUnreadCount() })
    }

    /**
     * @api {get} /notify 获取全部消息
     * @apiDescription  获取全部消息
     * @apiName getAllMsg
     * @apiVersion 0.0.1
     * @apiUse PageQuery
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {"pageNum": 0,"pageSize": 30,"total": 6,"item": [{"id": 1,"content": "nmka",
     * "title": "test","sendId": 5,"sendType": "mass","status": "progress","createTime": "2020-05-27T04:14:49.000+00:00",
     * "idList": null,"readStatus": "read"},{"id": 5,"content": "nmka2","title": "test","sendId": 5,"sendType": "mass",
     * "status": "progress","createTime": "2020-05-27T08:41:21.000+00:00","idList": null,"readStatus": "unread"},{"id": 6,
     * "content": "nmka2","title": "test","sendId": 5,"sendType": "assign","status": "progress","createTime":
     * "2020-05-27T08:41:26.000+00:00","idList": null,"readStatus": "unread"}]}}
     * @apiGroup Notify
     * @apiUse tokenMsg
     * @apiPermission user
     */
    @GetMapping
    fun getAllMsg(query: PageQuery): Mono<ResponseInfo<PageView<Notify>>> {
        return ResponseInfo.ok(mono { notifyService.getAllMsg(query) })
    }

    /**
     * @api {post} /notify 添加消息
     * @apiDescription  添加消息
     * @apiName addMsg
     * @apiVersion 0.0.1
     * @apiUse Notify
     * @apiParamExample {json} Request-Example:
     * {"content": "nmka2","title": "test","sendType" : "assign","idList": [5]}
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {}}
     * @apiGroup Notify
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping
    fun addMsg(@RequestBody msg: Mono<Notify>): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono { notifyService.addMsg(msg.awaitSingle()) })
    }

    /**
     * @api {get} /notify/announce 获取公告
     * @apiDescription  获取公告，只能获取一条
     * @apiName getAnnounce
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {}}
     * @apiGroup Notify
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @GetMapping("/announce")
    fun getAnnounce(): Mono<ResponseInfo<Notify>> {
        return ResponseInfo.ok(mono { notifyService.getAnnounce() })
    }

    /**
     * @api {delete} /notify 删除消息
     * @apiDescription  删除消息
     * @apiName deleteMsg
     * @apiVersion 0.0.1
     * @apiParam {Integer} id 消息id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {}}
     * @apiGroup Notify
     * @apiUse tokenMsg
     * @apiPermission supperAdmin
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @DeleteMapping
    fun deleteMsg(id: Int): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono { notifyService.deleteMsg(id) })
    }

    /**
     * @api {put} /notify 更新消息
     * @apiDescription  更新消息
     * @apiName updateMsg
     * @apiVersion 0.0.1
     * @apiUse Notify
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data": {}}
     * @apiGroup Notify
     * @apiUse tokenMsg
     * @apiPermission supperAdmin
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @PutMapping
    fun updateMsg(@RequestBody msg: Notify): Mono<ResponseInfo<Int>> {
        return ResponseInfo.ok(mono { notifyService.updateMsg(msg) })
    }

}