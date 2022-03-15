package com.mt.mtsocket.distribute


/**
 * Created by gyh on 2020/4/17.
 *
 * 该模块是基于Spring Mvc模式写的一个对webSocket的支持
 * 用法类似Spring Mvc
 * 该模块只支持{@code RequestMapping}、{@code RequestParam}和{@code RequestBody}注解，不支持参数得验证
 * 该模块还有优化空间
 *
 * 通讯请求格式国定为：
 * {"order":"请求接口", "data":"any", "req":0}
 * 
 */
