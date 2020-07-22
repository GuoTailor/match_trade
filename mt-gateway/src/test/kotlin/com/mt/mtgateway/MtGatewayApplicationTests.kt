package com.mt.mtgateway

import org.junit.jupiter.api.Test
import java.time.*
import java.util.*

//@SpringBootTest
class MtGatewayApplicationTests {

    @Test
    fun contextLoads() {
        val now = LocalDateTime.now()
        Thread.sleep(10)
        val end = LocalDateTime.now()
        val duration = Duration.between(end, now)
        println(duration.toMillis())
        println(now < end)
        val zoneOffset: ZoneOffset = ZoneId.systemDefault().rules.getOffset(Instant.now())
        println(Duration.ZERO.toMillis())
        println(end.minus(Duration.ZERO))
    }

    @Test
    fun testTreeSet() {
        val comparable = Comparator<User> { o1, o2 ->
            print(o1)
            println(o2)
            o1.age.compareTo(o2.age)
        }
        val treeSet = TreeSet(comparable)
        val user = User("1", 1)
        treeSet.add(User("2", 2))
        treeSet.add(user)
        treeSet.forEach { println(it) }
        user.age = 3
        println("-------------")
        println(treeSet.remove(user))
        println(treeSet.size)
    }

    @Test
    fun testList() {
        println(listOf("1", "2", "3").joinToString(prefix = "[", postfix = "]") { "\"$it\"" })
    }

    class User(var userName: String, var age: Int) {
        override fun equals(other: Any?): Boolean {
            println("equals")
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as User

            if (userName != other.userName) return false
            if (age != other.age) return false

            return true
        }

        override fun hashCode(): Int {
            println("hash")
            var result = userName.hashCode()
            result = 31 * result + age
            return result
        }

        override fun toString(): String {
            return "User(userName='$userName', age=$age)"
        }


    }

}
