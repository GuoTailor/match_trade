package com.mt.mtuser.service.room

import com.mt.mtuser.entity.room.BaseRoom
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/3/23.
 */
@Service
class BaseRoomService {
    val logger = LoggerFactory.getLogger(this.javaClass.simpleName)
    @Autowired
    protected lateinit var connect: DatabaseClient

    suspend fun getNextRoomId(room: BaseRoom) = getNextRoomId(RoomExtend.getRoomEnum(room.flag))

    /**
     * 获取下一个自增id
     */
    suspend fun getNextRoomId(roomFlag: RoomEnum): String {
        val result = connect.execute("select nextval('mt_room_seq')")
                .map { t, _ -> t.get("nextval", Integer::class.java) }
                .awaitOne()
        val roomId = roomFlag.flag + result
        logger.info("构建房间id:{}", roomId)
        return roomId
    }

}
