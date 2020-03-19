# match_trade 
注意事项：
- 该工程使用kotlin + webflux + postgresql + zookeeper
- 启动时请先启动zookeeper和postgresql
- 访问时请统一访问mt-gateway，85端口，然后由mt-gateway转发请求，<p>
因为请求的验证是在mt-gateway完成的，其他服务没有配置token解析的模块
- 如果要更改数据库的用户名密码记住mt-gateway里面也有数据库相关配置
 