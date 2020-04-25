# match_trade 

服务器返回的统一格式
```json
    {
      "code": 0,
      "msg":"成功或失败",
      "data": ["数据"]
    }
```
打包:进入到指定模块根目录运行` mvnw clean kotlin:compile package -Dmaven.test.skip=true ` <br>
运行:进入到指定模块根目录运行` mvnw spring-boot:run `

## 注意事项：
- 每次推代码时先拉一下调试没bug再推
- 该工程使用kotlin + webflux + postgresql + zookeeper
- 启动时请先启动zookeeper和postgresql
- 访问时请统一访问mt-gateway，85端口，然后由mt-gateway转发请求，<br>
因为请求的验证是在mt-gateway完成的，其他服务没有配置token解析的模块
- 如果要更改数据库的用户名密码记住mt-gateway里面也有数据库相关配置
- spring security PreAuthorize注解 与kotlin协程不兼容 [详情](https://github.com/spring-projects/spring-security/issues/8143)
- kotlin挂起函数不支持事务

## 关于学习文档：

<a style="color:red;"> 注意以下文档均需要翻墙访问，且为英文文档，可以用chrome搭配谷歌访问助手访问，chrome支持翻译网页</a>

- [kotlin文档](https://www.kotlincn.net/docs/reference/)
- [关于为什么在spring中使用kotlin](https://spring.io/blog/2019/04/12/going-reactive-with-spring-coroutines-and-kotlin-flow)
- [关于如可使用spring webflux](https://htmlpreview.github.io/?https://github.com/get-set/reactor-core/blob/master-zh/src/docs/index.html#which-operator)
- [关于如何在spring webflux中使用kotlin](https://docs.spring.io/spring/docs/5.2.0.M1/spring-framework-reference/languages.html#coroutines)
- [关于如何在spring webflux中使用R2DBC](https://docs.spring.io/spring-data/r2dbc/docs/1.1.0.M4/reference/html/#reference)

[再添加一篇关于 kotlin协程 与 Spring Reactor（Mono类）的互相转换](https://www.jianshu.com/p/17d93f1afc50)



## 关于持续集成一键部署


**前提说明**:使用一键部署的时候需要更改bootstrap.yml
- mt-gateway
```yaml
spring:
  cloud:
    # 使用 Nacos 作为配置中心
    nacos:
      config:
        group: DEFAULT_GROUP
        server-addr: nacos:8848
        file-extension: yml
        prefix: mt-gateway

  profiles:
    active: prod

```
- mt-user
```yaml
spring:
  cloud:
    # 使用 Nacos 作为配置中心
    nacos:
      config:
        group: DEFAULT_GROUP
        server-addr: nacos:8848
        file-extension: yml
        prefix: api
  profiles:
    active: prod
```

**使用gitlab持续集成一键部署**

1. 登录gitlab[登录入口](http://lian.yaolong.top:8070/)
   - 账号: root 
   - 密码:12345678

    ![](https://note.youdao.com/yws/api/personal/file/WEBef90f59563d9cb1e8e687d1b9619f5b9?method=download&shareKey=e1c9736515872eefd49d0fd60f1c81bd)

2. 添加ssh秘钥
    ![](https://note.youdao.com/yws/api/personal/file/WEB516de060d72a8d5a5836eed5fe0dc9d6?method=download&shareKey=fdb428160cd6759effb52a6f445e42a1)

    ![](https://note.youdao.com/yws/api/personal/file/WEBbcc5aeca3be8e855ef7962e34e14d61b?method=download&shareKey=469043c99e6428a54537b87311f69817)

3. 登录成功后找到项目

    ![](https://note.youdao.com/yws/api/personal/file/WEBd34595f2e097a68e2bd74f81124aee7a?method=download&shareKey=553778945483540d7daa75b9c78f1c28)

4. 复制ssh拉取地址
    ![](https://note.youdao.com/yws/api/personal/file/WEB8e507db30a197a69f707ba809b1b565a?method=download&shareKey=894b47232001e1d998550586024f7ab2)

5. 项目中添加ssh地址
    ![](https://note.youdao.com/yws/api/personal/file/WEB310abfd9061b1c36473114f690d649d6?method=download&shareKey=78c4c86f1e8a1dba8fd33b12d518d209)

    ![](https://note.youdao.com/yws/api/personal/file/WEB6ec72043ab69517e7abc280efbeb0c91?method=download&shareKey=346c85ff260eb9e0826fccbec1d41a86)

6. 直接选择刚才添加的地址推送

7. 推送成功后在gitlab上面查看部署的流水线（部署的过程）
    ![](https://note.youdao.com/yws/api/personal/file/WEB63613ac7fdacc22000caf5f66e9102a5?method=download&shareKey=2ae3ad14c1b70c49a7334b9bc58f8d43)

8. [nacos地址入口](http://liuyuelian.top:8848/nacos/)

9. [api文档地址](http://47.107.178.147/)

