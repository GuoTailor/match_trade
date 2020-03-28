# match_trade 

服务器返回的统一格式
```json
    {
      "code": 0,
      "msg":"成功或失败",
      "data": ["数据"]
    }
```
打包:进入到指定模块根目录运行``` mvnw clean kotlin:compile package -Dmaven.test.skip=true ``` <br>
运行:进入到指定模块根目录运行``` mvnw spring-boot:run ```

## 注意事项：
- 每次推代码时先拉一下调试没bug再推
- 该工程使用kotlin + webflux + postgresql + zookeeper
- 启动时请先启动zookeeper和postgresql
- 访问时请统一访问mt-gateway，85端口，然后由mt-gateway转发请求，<br>
因为请求的验证是在mt-gateway完成的，其他服务没有配置token解析的模块
- 如果要更改数据库的用户名密码记住mt-gateway里面也有数据库相关配置
- spring security PreAuthorize注解 与kotlin协程不兼容 [详情](https://github.com/spring-projects/spring-security/issues/8143)

## 关于学习文档：
    
<a style="color:red;"> 注意以下文档均需要翻墙访问，且为英文文档，可以用chrome搭配谷歌访问助手访问，chrome支持翻译网页</a>

- [kotlin文档](https://www.kotlincn.net/docs/reference/)
- [关于为什么在spring中使用kotlin](https://spring.io/blog/2019/04/12/going-reactive-with-spring-coroutines-and-kotlin-flow)
- [关于如可使用spring webflux](https://htmlpreview.github.io/?https://github.com/get-set/reactor-core/blob/master-zh/src/docs/index.html#which-operator)
- [关于如何在spring webflux中使用kotlin](https://docs.spring.io/spring/docs/5.2.0.M1/spring-framework-reference/languages.html#coroutines)
- [关于如何在spring webflux中使用R2DBC](https://docs.spring.io/spring-data/r2dbc/docs/1.1.0.M4/reference/html/#reference)

[再添加一篇关于 kotlin协程 与 Spring Reactor（Mono类）的互相转换](https://www.jianshu.com/p/17d93f1afc50)
 