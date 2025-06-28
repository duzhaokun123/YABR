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

## 编译

[`app/build.gradle.kts`](app/build.gradle.kts) 里的 `runtimeOnly(projects.loader.xxx)` 都是可选的
按需要修改

[`app/build.gradle.kts`](app/build.gradle.kts) 里的 `runtimeOnly(projects.hooker.xxx)` 也是可选的

但依赖从下表

| 环境                 | 建议 loader | 建议 hooker |
|---------------------|-------------|-----------|
| 大多数 legacy xposed | xposed      | xposed    |
| LSPosed             | xposed*     | xposed*   |
| Zygisk              | inline      | pine      |
| ReVanced**          | inline/acf  | pine      |
| Rxposed             | rxposed     | pine      |
| 其他注入方式          | inline      | pine      |

*: 虽然 xposed100 可用 但它看起来太丑了

**: ReVanced 兼容已被引入 androidx 破坏

## 开发

[模块功能开发](docs/模块功能开发.md)

## TODO

- 支持加载其他 xposed 模块
- 抄更多哔哩漫游的功能
