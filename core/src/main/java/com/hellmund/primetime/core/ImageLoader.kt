package com.hellmund.primetime.core

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import javax.inject.Inject

sealed class Transformation {
    object CenterCrop : Transformation()
    data class Placeholder(val resId: Int) : Transformation()
}

interface ImageLoader {
    fun load(
        url: String,
        into: ImageView,
        transformations: Array<Transformation> = arrayOf(Transformation.CenterCrop),
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
        transformations: Array<Transformation>,
        onComplete: ((Drawable) -> Unit)?,
        onError: (() -> Unit)?
    ) {
        val requestCreator = instance.load(url)

        for (transformation in transformations) {
            when (transformation) {
                is Transformation.CenterCrop -> requestCreator.fit().centerCrop()
                is Transformation.Placeholder -> requestCreator.placeholder(transformation.resId)
            }
        }

        requestCreator.into(into, object : Callback {
            override fun onSuccess() {
                onComplete?.invoke(into.drawable)
            }

            override fun onError(e: Exception?) {
                onError?.invoke()
            }
        })
    }

}
