package com.mt.mtuser.service.room

import com.mt.mtuser.dao.room.ClickRoomDao
import com.mt.mtuser.dao.room.DoubleRoomDao
import com.mt.mtuser.dao.room.TimelyRoomDao
import com.mt.mtuser.dao.room.TimingRoomDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/3/23.
 */
@Service
class BaseRoomService {
    @Autowired lateinit var clickRoomDao : ClickRoomDao
    @Autowired lateinit var doubleRoomDao : DoubleRoomDao
    @Autowired lateinit var timelyRoomDao : TimelyRoomDao
    @Autowired lateinit var timingRoomDao : TimingRoomDao

    fun createClickRoom() {

    }
}
