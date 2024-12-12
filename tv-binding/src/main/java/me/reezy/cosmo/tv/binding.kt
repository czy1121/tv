@file:Suppress("FunctionName")

package me.reezy.cosmo.tv

import androidx.databinding.BindingAdapter
import coil.imageLoader
import coil.request.ImageRequest


@BindingAdapter("android:text")
fun BindingAdapter_text(view: LiteTextView, value: String?) {
    view.text = value
}

@BindingAdapter("icon")
fun BindingAdapter_icon(view: LiteTextView, value: String?) {
    view.context.imageLoader.enqueue(ImageRequest.Builder(view.context).data(value).target {
        view.icon = it
    }.build())
}

@BindingAdapter("tvSubtext")
fun BindingAdapter_tvSubtext(view: SuperTextView, value: String?) {
    view.subtext = value
}

@BindingAdapter("tvIcon")
fun BindingAdapter_tvIcon(view: SuperTextView, value: String?) {
    view.context.imageLoader.enqueue(ImageRequest.Builder(view.context).data(value).target {
        view.icon = it
    }.build())
}