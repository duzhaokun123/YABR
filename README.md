# YABR

![Xposed Module](https://img.shields.io/badge/Xposed-Module-blue?style=flat-square)

Yet Another BiliRoaming

没什么用 只是证明哔哩漫游可以有更模块化的设计
这是一堆实验性很强的东西

属于是闲得慌了

## 功能

- 获取封面
- 全局 Dialog 系统模糊
- 多窗口支持
- ... (自己看源码)

## 编译

[`app/build.gradle.kts`](app/build.gradle.kts) 里的 `runtimeOnly(projects.loader.xxx)` 都是可选的
按需要修改

[`app/build.gradle.kts`](app/build.gradle.kts) 里的 `runtimeOnly(projects.hooker.xxx)` 也是可选的

但依赖从下表

| 环境                 | 建议 loader | 建议 hooker            |
|----------------------|-------------|----------------------|
| 大多数 legacy xposed | xposed      | xposed               |
| LSPosed              | libxposed*  | libxposed*, xposed** |
| Zygisk               | inline      | pine                 |
| Rxposed              | rxposed     | pine                 |
| 其他注入方式         | inline      | pine                 |

*: 未充分测试

**: 是的 可以在这么工作

## 开发

[模块功能开发](docs/模块功能开发.md)

## TODO

- 支持加载其他 xposed 模块
- 抄更多哔哩漫游的功能
- 统一数据类(gson, fastjson, kotlin data class, protobuf)访问修改器
- ThreePointHook 的回调使用原始数据的克隆 并另外提供修改方法
- fastjson 处理支持 `@JSONField(deserialize = false, serialize = false)`

## 下载

[github actions](https://github.com/duzhaokun123/YABR/actions/workflows/build.yml)

不建议
