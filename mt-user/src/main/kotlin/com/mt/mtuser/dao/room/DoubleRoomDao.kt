package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.DoubleMatch
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * Created by gyh on 2020/3/23.
 * 两两撮合
 */
interface DoubleRoomDao : ReactiveCrudRepository<DoubleMatch, Int>