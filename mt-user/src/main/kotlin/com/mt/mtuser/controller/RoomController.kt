package com.mt.mtuser.controller

import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.room.*
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * Created by gyh on 2020/3/25.
 */
@RestController
@RequestMapping("/room")
class RoomController {
    @Autowired
    lateinit var roomService: RoomService

    /**
     * @api {post} /room/click 创建一个点选撮合的房间
     * @apiDescription  创建一个点选撮合的房间
     * @apiName createClickRoom
     * @apiVersion 0.0.1
     * @apiUse ClickMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/click")
    @PreAuthorize("hasRole('ADMIN')")
    // @Valid 和 @Validated 注解均不起作用，不知道为什么
    fun createClickRoom(@RequestBody clickRoom: Mono<ClickMatch>): Mono<ResponseInfo<ClickMatch>> {
        return ResponseInfo.ok(mono {
            roomService.createRoom(clickRoom.awaitSingle())
        })
    }

    /**
     * @api {post} /room/bicker 创建一个抬杠撮合的房间
     * @apiDescription  创建一个点抬杠撮合的房间
     * @apiName createBickerRoom
     * @apiVersion 0.0.1
     * @apiUse BickerMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/bicker")
    @PreAuthorize("hasRole('ADMIN')")
    fun createBickerRoom(@RequestBody bickerRoom: Mono<BickerMatch>): Mono<ResponseInfo<BickerMatch>> {
        return ResponseInfo.ok(mono {
            roomService.createRoom(bickerRoom.awaitSingle())
        })
    }

    /**
     * @api {post} /room/double 创建一个两两撮合的房间
     * @apiDescription  创建一个两两撮合的房间
     * @apiName createDoubleRoom
     * @apiVersion 0.0.1
     * @apiUse DoubleMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/double")
    @PreAuthorize("hasRole('ADMIN')")
    fun createDoubleRoom(@RequestBody doubleRoom: Mono<DoubleMatch>): Mono<ResponseInfo<DoubleMatch>> {
        return ResponseInfo.ok(mono {
            roomService.createRoom(doubleRoom.awaitSingle())
        })
    }

    /**
     * @api {post} /room/continue 创建一个连续撮合的房间
     * @apiDescription  创建一个连续撮合的房间
     * @apiName createTimelyMatch
     * @apiVersion 0.0.1
     * @apiUse TimelyMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/continue")
    @PreAuthorize("hasRole('ADMIN')")
    fun createTimelyMatch(@RequestBody continueRoom: Mono<ContinueMatch>): Mono<ResponseInfo<ContinueMatch>> {
        return ResponseInfo.ok(mono {
            roomService.createRoom(continueRoom.awaitSingle())
        })
    }

    /**
     * @api {post} /room/timing 创建一个定时撮合的房间
     * @apiDescription  创建一个定时撮合的房间
     * @apiName createTimingMatch
     * @apiVersion 0.0.1
     * @apiUse TimingMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PostMapping("/timing")
    @PreAuthorize("hasRole('ADMIN')")
    fun createTimingMatch(@RequestBody timingRoom: Mono<TimingMatch>): Mono<ResponseInfo<TimingMatch>> {
        return ResponseInfo.ok(mono {
            roomService.createRoom(timingRoom.awaitSingle())
        })
    }

    /**
     * @api {put} /room/click 更新一个点选撮合的房间
     * @apiDescription  更新一个点选撮合的房间
     * @apiName updateClickRoom
     * @apiVersion 0.0.1
     * @apiUse ClickMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/click")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateClickRoom(@RequestBody clickRoom: Mono<ClickMatch>): Mono<ResponseInfo<ClickMatch>> {
        return ResponseInfo.ok(mono {
            roomService.updateRoomByRoomId(clickRoom.awaitSingle())
        })
    }

    /**
     * @api {put} /room/bicker 更新一个抬杠撮合的房间
     * @apiDescription  更新一个抬杠撮合的房间
     * @apiName updateBickerRoom
     * @apiVersion 0.0.1
     * @apiUse BickerMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/bicker")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateBickerRoom(@RequestBody bickerRoom: Mono<BickerMatch>): Mono<ResponseInfo<BickerMatch>> {
        return ResponseInfo.ok(mono {
            roomService.updateRoomByRoomId(bickerRoom.awaitSingle())
        })
    }

    /**
     * @api {put} /room/double 更新一个两两撮合的房间
     * @apiDescription  更新一个两两撮合的房间
     * @apiName updateDoubleRoom
     * @apiVersion 0.0.1
     * @apiUse DoubleMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/double")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateDoubleRoom(@RequestBody doubleRoom: Mono<DoubleMatch>): Mono<ResponseInfo<DoubleMatch>> {
        return ResponseInfo.ok(mono {
            roomService.updateRoomByRoomId(doubleRoom.awaitSingle())
        })
    }

    /**
     * @api {put} /room/continue 更新一个连续撮合的房间
     * @apiDescription  更新一个连续撮合的房间
     * @apiName updateTimelyMatch
     * @apiVersion 0.0.1
     * @apiUse TimelyMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/continue")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateTimelyMatch(@RequestBody continueRoom: Mono<ContinueMatch>): Mono<ResponseInfo<ContinueMatch>> {
        return ResponseInfo.ok(mono {
            roomService.updateRoomByRoomId(continueRoom.awaitSingle())
        })
    }

    /**
     * @api {put} /room/timing 更新一个定时撮合的房间
     * @apiDescription  更新一个定时撮合的房间
     * @apiName updateTimingMatch
     * @apiVersion 0.0.1
     * @apiUse TimingMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @PutMapping("/timing")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateTimingMatch(@RequestBody timingRoom: Mono<TimingMatch>): Mono<ResponseInfo<TimingMatch>> {
        return ResponseInfo.ok(mono {
            roomService.updateRoomByRoomId(timingRoom.awaitSingle())
        })
    }

    /**
     * @api {gut} /room/editable 获取全部可编辑的房间
     * @apiDescription  获取全部可编辑的房间，就是自己管理的房间
     * @apiName getEditableRoomList
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":[]}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @GetMapping("/editable")
    @PreAuthorize("hasRole('ADMIN')")
    fun getEditableRoomList(): Mono<ResponseInfo<LinkedList<BaseRoom>>> {
        return ResponseInfo.ok(mono {
            roomService.getEditableRoomList()
        })
    }

    //--------------------------------不需要管理员权限-------------------------------------

    /**
     * @api {gut} /room 获取全部的房间
     * @apiDescription  获取全部的房间，就是自己能加入的房间
     * @apiName getAllRoomList
     * @apiVersion 0.0.1
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":[]}
     * @apiGroup Room
     * @apiUse tokenMsg
     * @apiPermission admin
     */
    @GetMapping
    fun getAllRoomList(): Mono<ResponseInfo<LinkedList<BaseRoom>>> {
        return ResponseInfo.ok(mono {
            roomService.getAllRoomList()
        })
    }

}