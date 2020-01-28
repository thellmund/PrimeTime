package com.hellmund.primetime.core

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import javax.inject.Inject

interface ImageLoader {
    fun load(
        url: String,
        into: ImageView,
        placeholderResId: Int? = null,
        onComplete: ((Drawable) -> Unit)? = null,
        onError: (() -> Unit)? = null
    )
}

class PicassoImageLoader @Inject constructor(context: Context) : ImageLoader {

    private val instance: Picasso
        get() = Picasso.get()

    init {
        val instance = Picasso.Builder(context)
            .downloader(OkHttp3Downloader(context, Long.MAX_VALUE))
            .build()
        Picasso.setSingletonInstance(instance)
    }

    override fun load(
        url: String,
        into: ImageView,
        placeholderResId: Int?,
        onComplete: ((Drawable) -> Unit)?,
        onError: (() -> Unit)?
    ) {
        instance
            .load(url)
            .fit()
            .centerCrop()
            .apply {
                placeholderResId?.let {
                    placeholder(it)
                }
            }
            .into(into, object : Callback {
                override fun onSuccess() {
                    onComplete?.invoke(into.drawable)
                }

                override fun onError(e: Exception?) {
                    onError?.invoke()
                }
            })
    }
}
