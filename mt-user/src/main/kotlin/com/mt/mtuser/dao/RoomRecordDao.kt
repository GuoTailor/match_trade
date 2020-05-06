package com.mt.mtuser.dao

import com.mt.mtcommon.RoomRecord
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/3/26.
 */
interface RoomRecordDao : CoroutineCrudRepository<RoomRecord, Int>