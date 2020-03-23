package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.ClickMatch
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * Created by gyh on 2020/3/23.
 * 点选撮合
 */
interface ClickRoomDao : ReactiveCrudRepository<ClickMatch, Int>