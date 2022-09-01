# VipTime
[中文文档](README.md)

Viptime is a timing and delay function on the Android side. It can easily realize a timing task or a delay task through a few lines of code. It must be worth having without traditional constraints!

## Quick start

1、In the build.gradle file under your project, import maven。

```groovy
allprojects {
    repositories {
        maven { url "https://gitee.com/AbnerAndroid/almighty/raw/master" }
    }
}
```
2、In the build.gradle file of the module you need to use, import the dependency。
```groovy
dependencies {
    implementation 'com.vip:time:1.0.0'
}
```
## Basic Usage

1、Countdown (delay), the parameter is the time to be counted down, long type。
```kotlin
setTimeOut(5) {
     //Countdown complete
}
```

2、Countdown (delay) return time, parameter is the time to be counted down, long type。
```kotlin
 setTimeDown(5) {
    if (it == end) {
       //Countdown complete
    } else {
        val t= it.toString()//Countdown time value
    }
}
```
3、Timing, constraint time, and how often the interval is returned. The parameters are end (time required for timing), period (time interval, how often to poll)。
```kotlin
setInterval(10, 2) {
  if (it == end) {
         //Timed completion
    } else {
       val t = it.toString()//Timing time value
    }
}
```
4、 The infinite timing parameter period is the polling interval, that is, how often polling is performed
```kotlin
   setIntervalWireless(1) {
      val t = it.toString()//Timing time value
   }
```
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
