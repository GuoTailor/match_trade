package com.mt.mtuser.config

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.api.PostgresqlConnection
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement


/**
 * Created by gyh on 2020/3/15.
 */
@Configuration
@EnableR2dbcRepositories(basePackages = ["com.mt.mtuser.dao"])
@EnableTransactionManagement
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
    override fun connectionFactory(): ConnectionFactory {
        return PostgresqlConnectionFactory(PostgresqlConnectionConfiguration
                .builder()
                .host(host!!)
                .database(database)
                .username(username!!)
                .password(password)
                .port(port)
                .build())
    }

    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }

    @Bean
    fun connect(connectionFactory: ConnectionFactory): DatabaseClient {
        return DatabaseClient.create(connectionFactory)
    }
}