package com.mt.mtuser.controller

import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.room.ClickMatch
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/25.
 */
@RestController
@RequestMapping("/room")
class RoomController {
    @Autowired
    lateinit var roomService: RoomService

    /**
     * @api {post} /room/create/click 创建一个点选撮合的房间
     * @apiDescription  创建一个点选撮合的房间
     * @apiName createClickRoom
     * @apiVersion 0.0.1
     * @apiUse ClickMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiPermission admin
     */
    @PostMapping("/create/click")
    fun createClickRoom(@RequestBody @Validated clickRoom: ClickMatch): Mono<ResponseInfo<ClickMatch>> {
        return ResponseInfo.ok(mono{
            roomService.createClickRoom(clickRoom)
        })
    }
}