# BaseLoadingView

### 项目介绍
一个针对android在数据加载时使用到到的自定义控件，一行代码调用，包括多种模式，多种场景的适配，支持覆盖／悬浮显示，支持动画过渡，支持自定义样式，支持回调处理；

### 控件说明
1、一个小功能控件，使用方面，带状态切换过渡微交互效果，各种情况下都不会显得突兀；
2、扩展能力强，基本可以满足目前大部分App的阻塞过渡交互需求；
3、自定义小控件完全开源，旨在节省大家的开发时间，如有不足，望海涵！。


### 使用说明
1、将BaseLoadingView Copy到项目内；
#### 通过xml使用：
    <FrameLayout >

        <your content/>

        <.BaseLoadingView
               android:layout_width="match_parent"
               android:layout_height="match_parent"
               app:backgroundFill="@color/colorPrimary"
               app:backgroundOnAct="#2000"
               app:loadingRes="@drawable/progressbar_loading"
               app:noDataRes="@mipmap/base_nodata"
               app:noNetworkRes="@mipmap/base_nonetwork" />

    <FrameLayout >

##### backgroundFill :
###### 使loadingView遮住Content时，loadingView的背景色，默认为0xFFFEFEFE；
##### backgroundOnAct :
###### 使用loadingView并要求显示背景时（一般情况为上传图片、带缓存的页面加载等），loadingView的背景色，默认为透明度3-7%的五彩斑斓黑；
##### loadingRes :
###### 当调用展示Loading状态时，展示的drawable，建议使用AnimatorDrawable或自定义带动效的drawable；
##### noDataRes :
###### 当调用展示没有数据的状态时，展示的drawable；
##### noNetworkRes :
###### 当调用展示无网络连接时，展示的drawable；

#### 在Activity中使用（为简洁展示，代码以Kotlin书）：


    bld_view?.setRefreshEnable(boolean enable/*是否在无数据或无网络等容错情形出现 "点击页面以重新尝试" 的提示和刷新回调；默认true*/)

    bld_view?.setLoadingDrawable()
    bld_view?.setNoDataDrawable()
    bld_view?.setNoNetworkDrawable()

##### 显示／隐藏：
    /**
        *
        *@param mode: loading/noData/noNetwork/normal 四种模式，其中normal为隐藏；
        *@param hint:
        *@param isShowOnAct 是否显示在Activity上，
        *@param delayDismissTime 延迟消失时间，一般可用于做自定义Toast
        */
        blv?.setMode(BaseLoadingView.DisplayMode mode,String hint,boolean isShowOnAct)

        blv?.setMode(DisplayMode mode, String hint, boolean showOnAct, int delayDismissTime)


### 初始化无数据或网络失败时的callBack：
     blv?.setRefreshListener {
                //todo "call refresh with error"
                getData();
     }