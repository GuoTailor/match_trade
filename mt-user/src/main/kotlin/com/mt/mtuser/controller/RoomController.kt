package com.mt.mtuser.controller

import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.room.ClickMatch
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import javax.validation.Valid

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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    // @Valid 和 @Validated 注解均不起作用，不知道为什么
    fun createClickRoom(@RequestBody clickRoom: Mono<ClickMatch>): Mono<ResponseInfo<ClickMatch>> {
        return ResponseInfo.ok(mono{
            roomService.createRoom(clickRoom.awaitSingle())
        })
    }

    @PutMapping("/enable/{roomId}")
    fun enableRoom(@PathVariable roomId:String, @RequestParam value: String): Mono<ResponseInfo<Int>> {
        return ResponseInfo.ok(mono{
            roomService.enableRoom(roomId, value)
        })
    }
}