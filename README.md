# VipTime
[English documents](README_EN.md)
VipTime是一个Android端的定时和延时功能，通过寥寥几行代码便可轻松实现一个定时任务，或者一个延时任务，告别传统约束，必须值得拥有！

## 快速开始

1、在你的跟项目下的build.gradle文件下，引入maven。

```groovy
allprojects {
    repositories {
        maven { url "https://gitee.com/AbnerAndroid/almighty/raw/master" }
    }
}
```
2、在你需要使用的Module中build.gradle文件下，引入依赖。
```groovy
dependencies {
    implementation 'com.vip:time:1.0.0'
}
```
## 基本用法

1、倒计时（延时），参数为需要倒计时的时间，Long类型。
```kotlin
setTimeOut(5) {
     //倒计时完成
}
```

2、倒计时（延时）返回时间，参数为需要倒计时的时间，Long类型。
```kotlin
 setTimeDown(5) {
    if (it == end) {
       //倒计时完成
    } else {
        val t= it.toString()//倒计时 时间值
    }
}
```
3、定时，约束时间，间隔多少返回一次，参数为end(定时需要的时间), period(时间间隔，多久轮询一次)。
```kotlin
setInterval(10, 2) {
  if (it == end) {
         //定时完成
    } else {
       val t = it.toString()//定时 时间值
    }
}
```
4、 无限定时 参数period，为轮询间隔，也就是多久轮询一次
```kotlin
   setIntervalWireless(1) {
      val t = it.toString()//定时 时间值
   }
```

##Demo效果预览

<img src="image/time.png" width="32%" />

## 欢迎关注作者

微信扫描关注，查阅更多技术文章！

<img src="image/abner.jpg" width="200px" />

## License

```
Copyright (C) AbnerMing, VipTime Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
