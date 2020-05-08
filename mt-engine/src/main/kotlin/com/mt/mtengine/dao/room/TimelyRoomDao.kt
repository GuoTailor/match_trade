package com.mt.mtengine.dao.room

import com.mt.mtengine.entity.room.BaseRoom
import com.mt.mtengine.entity.room.TimelyMatch
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/23.
 * 及时撮合
 */
interface TimelyRoomDao : BaseRoomDao<TimelyMatch, String> {

    @Query("select count(1) from mt_room_timely where room_id = :roomId limit 1")
    override fun existsByRoomId(roomId: String): Mono<Int>

    @Modifying
    @Query("update mt_room_timely set enable = :enable where room_id = :roomId")
    override fun enableRoomById(roomId: String, enable: String): Mono<Int>

    @Query("select count(1) from mt_room_timely where company_id = :companyId")
    fun countByCompanyId(companyId: Int): Mono<Int>

    @Query("select * from mt_room_timely where room_id = :roomId")
    override fun findByRoomId(roomId: String): Mono<TimelyMatch>

    @Query("select * from mt_room_timely where company_id in (:companyId)")
    override fun findByCompanyIdAll(companyId: Iterable<Int>): Flux<TimelyMatch>

    @Query("select room_id, time, enable, start_time from mt_room_timely")
    override fun findTimeAll(): Flux<TimelyMatch>


    @Query("select company_id, stock_id from mt_room_timely where room_id = :roomId")
    override fun findBaseByRoomId(roomId: String): Mono<TimelyMatch>
}