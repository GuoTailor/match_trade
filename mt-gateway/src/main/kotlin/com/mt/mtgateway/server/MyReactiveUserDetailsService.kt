package com.mt.mtgateway.server

import com.mt.mtgateway.RoleRepository
import com.mt.mtgateway.User
import com.mt.mtgateway.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.lang.IllegalStateException


/**
 * Created by gyh on 2020/3/15.
 */
@Service
class MyReactiveUserDetailsService  {

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var roleRepository: RoleRepository

    fun findByUsername(username: String): Mono<User> {
        val user = userRepository.findByUsername(username)
                .switchIfEmpty(Mono.defer { Mono.error<User>(IllegalStateException("User Not Found")) })
        val roles = user.flatMapMany {
            roleRepository.findRoleByUserId(it.id!!).map {r -> r.name }
        }
        return Mono.zip(user, roles.collectList()) { u, r ->
            u.roles = r
            u
        }
    }
}