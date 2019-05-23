package com.hellmund.primetime.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import javax.inject.Inject

sealed class Transformation {
    object CenterCrop : Transformation()
    data class Placeholder(val resId: Int) : Transformation()
}

class ImageLoader @Inject constructor(context: Context) {

    private val requestManager: RequestManager = Glide.with(context)

    @JvmOverloads
    fun load(
            url: String,
            into: ImageView,
            transformations: Array<Transformation> = arrayOf(Transformation.CenterCrop),
            onComplete: ((Drawable) -> Unit)? = null,
            onError: (() -> Unit)? = null
    ) {
        val requestOptions = RequestOptions()

        transformations.forEach {
            requestOptions.apply(when (it) {
                is Transformation.Placeholder -> RequestOptions.placeholderOf(it.resId)
                is Transformation.CenterCrop -> RequestOptions.centerCropTransform()
            })
        }

        requestManager
                .load(url)
                .apply(requestOptions)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                    ): Boolean {
                        onError?.invoke()
                        return true
                    }

                    override fun onResourceReady(
                            resource: Drawable,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                    ): Boolean {
                        onComplete?.invoke(resource)
                        return false
                    }
                })
                .into(into)
    }

    companion object {
        @JvmStatic
        fun with(context: Context) = ImageLoader(context)
    }

}
