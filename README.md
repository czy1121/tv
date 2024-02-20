# tv

一些实用的自定义TextView

- TwoTextView - 两个文本一个图标，简化布局层级
  - 可额外设置一个文本(text2)，可设置文本位置(text2Gravity)
  - 可设置一个图标(icon)，可设置图标位置(iconGravity)
- LiteTextView - 单行文本一个图标，简化版 TextView 
  - 支持文本属性(text/textColor/textSize/fontFamily/gravity)
  - 支持设置图标(icon/iconTint/iconSize/iconPadding/iconGravity) 
- MarqueeTextView - 文本跑马灯效果(TextView 的跑马灯执行的条件过高)
  - 支持文本属性(text/textColor/textSize/fontFamily)
- CamelTextView - 可额外设置两个文本(prefix/suffix)，可用于带单位的数值，比如：<sub>￥</sub><b>123.0</b><sub>元</sub>
- ExpandableTextView - 可展开收缩的的文本，点击切换状态，右下角显示状态图标(展开/收缩)
- ReadMoreTextView - 可展开收缩的的文本，点击切换状态，尾部显示状态文本(展开/收缩) 

 

## Gradle

``` groovy
repositories {
    maven { url "https://gitee.com/ezy/repo/raw/cosmo/"}
}
dependencies {
    implementation "me.reezy.cosmo:tv:0.9.0"
}
```

## LICENSE

The Component is open-sourced software licensed under the [Apache license](LICENSE).