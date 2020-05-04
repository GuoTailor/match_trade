package com.mt.mtengine.dao.room

import com.mt.mtengine.entity.room.BaseRoom
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/25.
 */
interface BaseRoomDao<T : BaseRoom, ID> : ReactiveCrudRepository<T, ID> {

    fun existsByRoomId(roomId: String): Mono<Int>

    /**
     * 通过房间id启用/禁用房间
     */
    fun enableRoomById(roomId: String, enable: String): Mono<Int>

    fun findByRoomId(roomId: String): Mono<T>

    fun findByCompanyIdAll(companyId: Iterable<Int>): Flux<T>

    fun findTimeAll(): Flux<T>



    fun findBaseByRoomId(roomId: String): Mono<T>
}