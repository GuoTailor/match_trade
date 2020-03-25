package com.mt.mtuser.entity.room

/**
 * Created by gyh on 2020/3/24.
 */
interface BaseRoom<T : BaseRoom<T>> {
    var id: Int?
    var roomNumber: String?

}