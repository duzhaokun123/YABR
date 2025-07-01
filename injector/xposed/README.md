这是通过 xposed 模块加载 YABR 模块的实现

一般应该使用 YABR 本身做 xposed 模块加载
除非你使用的框架难以替换加载的模块

会调用 inline loader

不要重复加载 一定会出问题