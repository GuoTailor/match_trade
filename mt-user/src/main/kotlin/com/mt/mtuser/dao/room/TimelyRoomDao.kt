package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.TimelyMatch
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * Created by gyh on 2020/3/23.
 * 及时撮合
 */
interface TimelyRoomDao : ReactiveCrudRepository<TimelyMatch, Int>