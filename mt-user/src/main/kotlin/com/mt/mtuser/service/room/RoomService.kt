package com.mt.mtuser.service.room

import com.mt.mtuser.dao.room.ClickRoomDao
import com.mt.mtuser.dao.room.DoubleRoomDao
import com.mt.mtuser.dao.room.TimelyRoomDao
import com.mt.mtuser.dao.room.TimingRoomDao
import com.mt.mtuser.entity.room.ClickMatch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.kotlin.core.publisher.toMono

/**
 * Created by gyh on 2020/3/23.
 */
@Service
class RoomService {
    @Autowired lateinit var clickRoomDao : ClickRoomDao
    @Autowired lateinit var doubleRoomDao : DoubleRoomDao
    @Autowired lateinit var timelyRoomDao : TimelyRoomDao
    @Autowired lateinit var timingRoomDao : TimingRoomDao

    fun createClickRoom(clickRoom: ClickMatch) {
        clickRoom.toMono()
    }
}