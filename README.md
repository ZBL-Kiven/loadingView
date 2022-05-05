<p align="center" >
   <img src = "https://github.com/ZBL-Kiven/loadingView/raw/master/demo/title.png"/>
   <br>
   <a href = "https://developer.android.google.cn/">
      <img src = "https://img.shields.io/static/v1?label=platform&message=Android&color=6bf"/>
   </a>
   <a href = "https://github.com/ZBL-Kiven">
      <img src = "https://img.shields.io/static/v1?label=author&message=ZJJ&color=9cf"/>
  </a>
  <a href = "https://github.com/ZBL-Kiven/loadingView/raw/master/demo/demo.apk">
      <img src = "https://img.shields.io/static/v1?label=Newest&message=1.3.1&color=cce"/>
  </a>
</p>


## Introduction：

###### LoadingView 旨在为开发快速集成超轻量级加载过渡框架，很多项目中因协作开发或开发水平的参差不齐，导致在进行快速敏捷开发过程中出现五花八门的过渡效果甚至没有过渡动画，对用户体验造成极大的影响。该控件可在任何场景下实现页面的优雅过渡，且经过特殊算法处理后，在动画切换期间不会出现间断，使用效果非常棒。


## Features：

* 支持：默认 Loading 、Empty、Error 、Normal (None) 四种宏观切换场景。
* 支持：动画过渡。
* 支持：文字、图片、资源、内容、动画 等的自定义。
* 支持：Delay 设置。
* 支持：全屏、悬浮框、全屏 + 悬浮框 的弹出模式。
* 支持：动画 时长自定义、效果自定义、节点回退等。
* 支持：layout 布局/动态添加 使用。

## demo：

使用 Android 设备下载 [APK](https://github.com/ZBL-Kiven/loadingView/raw/master/demo/demo.apk) 安装包安装 demo 即可把玩。

## Installation



> 通过 Gradle 引入

```groovy
dependencies {
    implementation 'com.zj.repo:loading:1.3.0'
}
```

> 下载 [AAR]() 并导入项目 Lib

## Usage:
1、通过xml使用：

```xml
<xx.xx.xx.(extends:ZLoadingView)
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:backgroundFill="#a000" //刷新时整体背景
        app:backgroundUnderTheContent="@drawable/loading_float_background" //弹窗背景
        app:changeAnimDuration="300" //动画切换时长
        app:contentPadding="12dp" //弹窗模式下生效，弹窗边距，可单独设置各方向。
        app:drawerHeight="55dp"// 图标的高度
        app:drawerWidth="55dp" // 图标的宽度
        app:hintColor="#e3e3e3" //提示文本的字色
        app:hintTextSize="22sp" //提示文本的字号
        app:loadingText="@string/loading" //加载中提示文本
        app:maxRefreshTextLines="2" //最大刷新文本行数
        app:maxRefreshTextWidth="255dp" //最大刷新文本宽度
        app:modeDefault="LOADING" //默认模式，将在初始化显示，或用于调试
        app:networkErrorText="@string/noNetwork" //网络异常提示文本
        app:noDataText="@string/noData" //无数据提示文本
        app:refreshEnable="true" //是否允许刷新（影响点击及 BtnEnable）
        app:refreshNetworkText="check your network and try again" // 刷新提示网络异常文本
        app:refreshNoDataText="@string/refresh" // 提示无数据文本
        app:refreshTextColor="#b7b7b7" // 刷新提示文本的字色
        app:refreshTextSize="16sp" // 刷新提示文本的字号
        app:shownMode="fo" // 弹出模式，弹窗、全屏、全屏+弹窗。
/>
```

* backgroundFill :使 loadingView 遮住 Content 时，loadingView 的背景;

* backgroundUnderTheContent :使用 loadingView 并要求显示背景时（一般情况为上传图片、带缓存的页面加载等），loadingView 的背景；

* shownMode：其表示弹出 Loading 的样式， 其中包含以下枚举值：

  > Floating : 控件将以一个弹窗的样式出现，背景为  backgroundUnderTheContent 设置的样式。
  >
  > Overlap ：控件将整体出现，并覆盖内容，背景为 backgroundFill 设置的样式。
  >
  > fo = overlap + floating

2、ZLoadingView 是抽象的，即默认不可用的，因此我贴心的为你提供了两种内置方案：

> ZProgressLoadingView

```xml
< new-properties:
     app:progress_layout="@layout/test_pink_pb" // ProgressBar 填充资源
     app:progress_noDataRes="@mipmap/blv_no_data_white" //无数据时的图片
     app:progress_noNetworkRes="@mipmap/blv_network_error_white" //网络异常时的图片
/>
```

>ZRotateLoadingView

```xml
< new-properties
     app:rotate_loadingRes="@mipmap/blv_loading_white" //这张图片能自动旋转。
     app:rotate_noDataRes="@mipmap/blv_no_data_white" //无数据时的图片
     app:rotate_noNetworkRes="@mipmap/blv_network_error_white" //网络异常时的图片
/>
```

3、制定自己的刷新控件：

```java
class CusLoadingView extends ZLoadingView<SurfaceView, ImageView, TextView> {
  // 泛型可以设置为任意类型，在抽象中实现。
  
  @Override
    public void inflateLoadingView(ViewStub stub, float drawerWidth, float drawerHeight) {
      // 使 stub 填充任意 Layout ，作为 Loading View 的布局。
    }

    @Override
    public void inflateNoDataView(ViewStub stub, float drawerWidth, float drawerHeight) {
      // 使 stub 填充任意 Layout ，作为 noData 的布局。
    }

    @Override
    public void inflateNetworkErrorView(ViewStub stub, float drawerWidth, float drawerHeight) {
      // 使 stub 填充任意 Layout ，作为 networkHint 的布局。
    }

    @Override
    protected void onViewInflated() {
      //此处完成了所有的初始化，所有的 View 都可正常访问。
    }
}
```

4、状态切换

```java
/**
 * @param mode: loading/noData/noNetwork/normal 四种模式，其中normal为隐藏；
 * @param hint:
 * @param isShowOnAct 是否显示在Activity上，
 * @param delayDismissTime 延迟消失时间，一般可用于做自定义Toast
* */  
blv?.setMode(BaseLoadingView.DisplayMode mode,String hint,boolean isShowOnAct)

blv?.setMode(DisplayMode mode, String hint, boolean showOnAct, int delayDismissTime)

blv?.setRefreshListener {
    //todo "call refresh with error" , eg: getData();
}

//是否在无数据或无网络等容错情形出现 "点击页面以重新尝试" 的提示和刷新回调；默认 true 显示 
bld_view?.setRefreshEnable(boolean enable)

//延迟生效。延迟效果可在未生效时被新的状态或新的延时取缔。
blv?.setMode(DisplayMode.delay(Long))
```

### Contributing

Contributions are very welcome 🎉

### Licence :

Copyright (c) 2019 CityFruit zjj0888@gmail.com<br>
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.<br>
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.