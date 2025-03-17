# tv

一些实用的自定义TextView

- SuperTextView - 两个文本一个图标，简化布局层级
  - 支持设置一个图标(icon/iconTint/iconSize/iconPadding/iconGravity)
  - 支持设置一个次要文本(subtext/subtextColor/subtextSize/subtextFont/subtextStyle/subtextGravity)
  - 支持文字描边
  - 支持文字渐变色(从上到下渐变)
  - 支持用尺寸设置字间距(和蓝湖等设计工具保持一致)
- LiteTextView - 单行文本+可选图标，继承自 View 比 TextView 更轻量
  - 支持文本属性(text/textColor/textSize/fontFamily/textStyle/gravity)
  - 支持设置图标(icon/iconTint/iconSize/iconPadding/iconGravity) 
- MarqueeTextView - 文本跑马灯效果(TextView 的跑马灯执行的条件过高)
  - 支持文本属性(text/textColor/textSize/fontFamily)  
- ReadMoreTextView - 可展开收起的的文本，点击切换状态，尾部显示状态文本(展开/收起) 


<img src="img_1.png" width="360" />

<img src="img_2.png" width="360" />

<img src="img_3.png" width="360" />
 

## Gradle

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:tv-lite:0.10.10"
    implementation "me.reezy.cosmo:tv-super:0.10.14"
    implementation "me.reezy.cosmo:tv-marquee:0.10.10" 
    implementation "me.reezy.cosmo:tv-readmore:0.10.14" 
}
```

## 文字描边

SuperTextView 支持文字描边，通过 `Paint.Style.STROKE` 和 `Paint.Style.FILL` 先后绘制描边与文字

由于 `Paint.Style.STROKE` 绘制描边时是由路径向两边扩展的，也就是在绘制时描边与文字会有部分重叠，这会导致两个问题

- 绘制的描边宽度只是设置值的一半，可通过将描边宽度*2解决
- 当绘制的文字带透明度时，重叠部分会很明显，导致与UI设计不一致     

#### 另一种描边绘制模式

当文字带透明度时，为了精确还原UI，可尝试另一种描边绘制模式

`app:tvStrokeMode="path"`

首先，通过 `paint.getTextPath()` 获取到文本路径  
然后，通过 `canvas.clipOutPath(path)` 排除与文字重叠的部分  
最后，绘制描边 `canvas.drawPath(path, paint)`  

但此方法也有些问题

- 由于只能获取到不带格式的文本路径，所以当为 `Spanned` 绘制描边时可能会错位
- 在部分设备上绘制的描边可能会错位(可能是在 android 15+ 受 letterSpacing 影响)

## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).