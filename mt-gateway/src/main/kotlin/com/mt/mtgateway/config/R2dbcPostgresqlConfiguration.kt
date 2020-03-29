package com.mt.mtgateway.config

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration

import io.r2dbc.postgresql.PostgresqlConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration

import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories


/**
 * Created by gyh on 2020/3/15.
 */
@Configuration
@EnableR2dbcRepositories(basePackages = ["com.mt.mtgateway"])
@RefreshScope
class R2dbcPostgresqlConfiguration : AbstractR2dbcConfiguration() {
    @Value("\${datasource.host}")
    private val host: String? = null

    @Value("\${datasource.port}")
    private val port = 0

    @Value("\${datasource.database}")
    private val database: String? = null

    @Value("\${datasource.username}")
    private val username: String? = null

    @Value("\${datasource.password}")
    private val password: String? = null

    @Bean
    override fun connectionFactory(): PostgresqlConnectionFactory {
        return PostgresqlConnectionFactory(PostgresqlConnectionConfiguration
                .builder()
                .host(host!!)
                .database(database)
                .username(username!!)
                .password(password)
                .port(port)
                .build())
    }
}
