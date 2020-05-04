package com.mt.mtcommon

/**
 * Created by gyh on 2020/5/4.
 */
class RivalInfo(val userId: Int? = null,
                val roomId: String? = null,
                val rivals: Array<Int>? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RivalInfo

        if (userId != other.userId) return false
        if (rivals != null) {
            if (other.rivals == null) return false
            if (!rivals.contentEquals(other.rivals)) return false
        } else if (other.rivals != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId ?: 0
        result = 31 * result + (rivals?.contentHashCode() ?: 0)
        return result
    }
}