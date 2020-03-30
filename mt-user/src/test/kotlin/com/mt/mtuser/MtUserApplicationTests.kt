package com.mt.mtuser

import com.mt.mtuser.entity.User
import com.mt.mtuser.service.UserService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class MtUserApplicationTests {
    @Autowired
    private lateinit var userService: UserService

    @Test
    fun contextLoads() {
    }

}
