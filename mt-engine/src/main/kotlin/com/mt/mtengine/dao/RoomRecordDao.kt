package com.mt.mtengine.dao

import com.mt.mtcommon.RoomRecord
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository

/**
 * Created by gyh on 2020/3/26.
 */
interface RoomRecordDao : ReactiveCrudRepository<RoomRecord, Int>