<p align="center" >
   <img src = "https://github.com/ZBL-Kiven/loadingView/raw/master/demo/title.png"/>
   <br>
   <a href = "http://cityfruit.io/">
   <img src = "https://img.shields.io/static/v1?label=By&message=CityFruit.io&color=2af"/>
   </a>
   <a href = "https://github.com/ZBL-Kiven/loadingView">
      <img src = "https://img.shields.io/static/v1?label=platform&message=Android&color=6bf"/>
   </a>
   <a href = "https://github.com/ZBL-Kiven">
      <img src = "https://img.shields.io/static/v1?label=author&message=ZJJ&color=9cf"/>
  </a>
  <a href = "https://developer.android.google.cn/jetpack/androidx">
      <img src = "https://img.shields.io/static/v1?label=supported&message=AndroidX&color=8ce"/>
  </a>
  <a href = "https://www.android-doc.com/guide/components/android7.0.html">
      <img src = "https://img.shields.io/static/v1?label=minVersion&message=Nougat&color=cce"/>
  </a>
</p>
 
## Introduction：

###### LoadingView 旨在为开发快速集成超轻量级加载过渡框架，很多项目中因协作开发或开发水平的参差不齐，导致在进行快速敏捷开发过程中出现五花八门的过渡效果甚至没有过渡动画，对用户体验造成极大的影响。该控件可在任何场景下实现页面的优雅过渡，且经过特殊算法处理后，在动画切换期间不会出现间断，使用效果非常棒。


## Features：

* 支持：loading 、error、no-data 三种宏观切换场景。
* 支持：支持动画过渡。
* 支持：支持 文字/图片 资源自定义。
* 支持：支持动画自定义。
* 支持：支持背景色或背景图片自定义。
* 支持：支持 全屏/悬浮 模式。
* 支持：动画回退。
* 支持：layout 布局/动态添加 使用。

> 单元测试

- 即将覆盖

## demo：

使用 Android 设备下载 [APK](https://github.com/ZBL-Kiven/loadingView/raw/master/demo/demo.apk) 安装包安装 demo 即可把玩。

## Installation :


BaseLoadingView 已发布至私有仓库，你可以使用如下方式安装它：

> by dependencies:

```
repo{
     maven (url = "https://nexus.i-mocca.com/repository/cf_core")
}

implementation 'com.cf.core:loading:+'

```

> by [aar](https://nexus.i-mocca.com/repository/cf_core/com/cf/core/loading/1.0.0/loading-1.0.0.aar) import:

```
copy the aar file in your app libs to use
```

> by [module](https://github.com/ZBL-Kiven/loadingView/archive/master.zip) copy:
 
```
copy the module 'zj_loading' into your app

implementation project(":zj_loading")

```

## Usage:
> 通过xml使用：
 
```xml
<com.zj.loading.BaseLoadingView
            android:id="@+id/bld_view"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:loadingRes="@drawable/loading_progressbar"
            app:noDataRes="@mipmap/no_data"
            app:noNetworkRes="@mipmap/no_network"
            app:backgroundFill="@color/loading_color_background"
            app:backgroundUnderTheContent="@drawable/loading_corner_bg"
            app:hintColor="@color/colorPrimaryDark"
            app:refreshTextColor="@color/grayish"
            app:loadingText="@string/loading"
            app:refreshText="@string/refresh"
            app:networkErrorText="@string/noNetwork"
            app:noDataText="@string/noData"/>
```

* backgroundFill :使 loadingView 遮住 Content 时，loadingView 的背景;

* backgroundUnderTheContent :使用 loadingView 并要求显示背景时（一般情况为上传图片、带缓存的页面加载等），loadingView 的背景；

* loadingRes :当调用展示Loading状态时，展示的 drawable，建议使用 AnimatorDrawable 或自定义带动效的 drawable；

* noDataRes :当调用展示没有数据的状态时，展示的 drawable。

* noNetworkRes :当调用展示无网络连接时，展示的 drawable。

* hintColor :提示字体的颜色，如：‘加载中，请稍候...’ 的字体颜色 。

* refreshTextColor :需要刷新时，提示文本的颜色，如：轻触页面重新加载。

* loadingText :如：‘加载中，请稍候...’。

* refreshText :如：轻触页面重新加载e。

* noDataText :如：暂无数据。

* networkErrorText :如：网络链接失败。

> 在Activity中使用 ：
 
```
* @param mode: loading/noData/noNetwork/normal 四种模式，其中normal为隐藏；
* @param hint:
* @param isShowOnAct 是否显示在Activity上，
* @param delayDismissTime 延迟消失时间，一般可用于做自定义Toast
    
blv?.setMode(BaseLoadingView.DisplayMode mode,String hint,boolean isShowOnAct)

blv?.setMode(DisplayMode mode, String hint, boolean showOnAct, int delayDismissTime)

blv?.setRefreshListener {
            //todo "call refresh with error"
            getData();
}

//是否在无数据或无网络等容错情形出现 "点击页面以重新尝试" 的提示和刷新回调；默认 true 显示 
bld_view?.setRefreshEnable(boolean enable)

//也可以随时使用如下方法重新配置
bld_view?.setLoadingDrawable()
bld_view?.setNoDataDrawable()
bld_view?.setNoNetworkDrawable()
...  

```
### Contributing

Contributions are very welcome 🎉

### Licence :  

Copyright (c) 2019 CityFruit zjj0888@gmail.com<br>
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.<br>
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
