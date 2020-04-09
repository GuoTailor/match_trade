package com.mt.mtuser.controller

import com.mt.mtuser.entity.ResponseInfo
import com.mt.mtuser.entity.room.*
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*
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
     * @api {post} /room/click 创建一个点选撮合的房间
     * @apiDescription  创建一个点选撮合的房间
     * @apiName createClickRoom
     * @apiVersion 0.0.1
     * @apiUse ClickMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
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
     * @api {post} /room/bicker 创建一个点选撮合的房间
     * @apiDescription  创建一个点选撮合的房间
     * @apiName createBickerRoom
     * @apiVersion 0.0.1
     * @apiUse BickerMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
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
     * @api {post} /room/timely 创建一个及时撮合的房间
     * @apiDescription  创建一个及时撮合的房间
     * @apiName createTimelyMatch
     * @apiVersion 0.0.1
     * @apiUse TimelyMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiPermission admin
     */
    @PostMapping("/timely")
    @PreAuthorize("hasRole('ADMIN')")
    fun createTimelyMatch(@RequestBody timelyRoom: Mono<TimelyMatch>): Mono<ResponseInfo<TimelyMatch>> {
        return ResponseInfo.ok(mono {
            roomService.createRoom(timelyRoom.awaitSingle())
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
     * @api {put} /room/bicker 更新一个点选撮合的房间
     * @apiDescription  更新一个点选撮合的房间
     * @apiName updateBickerRoom
     * @apiVersion 0.0.1
     * @apiUse BickerMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
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
     * @api {put} /room/timely 更新一个及时撮合的房间
     * @apiDescription  更新一个及时撮合的房间
     * @apiName updateTimelyMatch
     * @apiVersion 0.0.1
     * @apiUse TimelyMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiPermission admin
     */
    @PutMapping("/timely")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateTimelyMatch(@RequestBody timelyRoom: Mono<TimelyMatch>): Mono<ResponseInfo<TimelyMatch>> {
        return ResponseInfo.ok(mono {
            roomService.updateRoomByRoomId(timelyRoom.awaitSingle())
        })
    }

    /**
     * @api {put} /room/timing 更新一个点定时撮合的房间
     * @apiDescription  更新一个定时撮合的房间
     * @apiName updateTimingMatch
     * @apiVersion 0.0.1
     * @apiUse TimingMatch
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
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
     * @apiPermission admin
     */
    @PutMapping("/editable")
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
     * @apiPermission admin
     */
    @PutMapping
    fun getAllRoomList(): Mono<ResponseInfo<LinkedList<BaseRoom>>> {
        return ResponseInfo.ok(mono {
            roomService.getAllRoomList()
        })
    }

    /**
     * @api {get} /room/enter 进入房间通过房间号
     * @apiDescription  进入房间通过房间号
     * @apiName enableRoom
     * @apiVersion 0.0.1
     * @apiParam {String} roomId 房间id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiPermission user
     */
    @GetMapping("/enter")
    fun enterRoom(@RequestParam roomId: String): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono {
            roomService.enterRoom(roomId)
        })
    }

    /**
     * @api {get} /room/quit 退出房间通过房间号
     * @apiDescription  退出房间通过房间号
     * @apiName quitRoom
     * @apiVersion 0.0.1
     * @apiParam {String} roomId 房间id
     * @apiSuccessExample {json} 成功返回:
     * {"code": 0,"msg": "成功","data":null}
     * @apiGroup Room
     * @apiPermission user
     */
    @GetMapping("/quit")
    fun quitRoom(roomId: String): Mono<ResponseInfo<Unit>> {
        return ResponseInfo.ok(mono {
            roomService.quitRoom(roomId)
        })
    }

}