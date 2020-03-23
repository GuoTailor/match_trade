package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.TimingMatch
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * Created by gyh on 2020/3/23.
 * 定时撮合
 */
interface TimingRoomDao : ReactiveCrudRepository<TimingMatch, Int> {
}