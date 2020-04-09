package com.mt.mtsocket.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement


/**
 * Created by gyh on 2020/3/15.
 */
@Configuration
@EnableR2dbcRepositories(basePackages = ["com.mt.mtsocket.dao"])
@EnableTransactionManagement
class R2dbcPostgresqlConfiguration  {
}
