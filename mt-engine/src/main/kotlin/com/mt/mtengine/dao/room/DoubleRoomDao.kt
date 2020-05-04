package com.mt.mtengine.dao.room

import com.mt.mtengine.entity.room.BaseRoom
import com.mt.mtengine.entity.room.DoubleMatch
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/23.
 * 两两撮合
 */
interface DoubleRoomDao : BaseRoomDao<DoubleMatch, String> {

    @Query("select count(1) from mt_room_double where room_id = :roomId limit 1")
    override fun existsByRoomId(roomId: String): Mono<Int>

    @Modifying
    @Query("update mt_room_double set enable = :enable where room_id = :roomId")
    override fun enableRoomById(roomId: String, enable: String): Mono<Int>

    @Query("select count(1) from mt_room_double where company_id = :companyId")
    fun countByCompanyId(companyId: Int): Mono<Int>

    @Query("select * from mt_room_double where room_id = ：roomId")
    override  fun findByRoomId(roomId: String): Mono<DoubleMatch>

    @Query("select * from mt_room_double where company_id in (:companyId)")
    override fun findByCompanyIdAll(companyId: Iterable<Int>): Flux<DoubleMatch>

    @Query("select room_id, time, enable, start_time from mt_room_double")
    override fun findTimeAll(): Flux<DoubleMatch>

    @Query("select company_id, stock_id from mt_room_double where room_id = :roomId")
    override fun findBaseByRoomId(roomId: String): Mono<DoubleMatch>
}