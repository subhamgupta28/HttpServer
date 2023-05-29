package com.subhamgupta.httpserver.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.Downsampler
import com.bumptech.glide.request.RequestOptions
import java.io.ByteArrayOutputStream
import java.io.File

fun File.getDirectChildrenCount(countHiddenItems: Boolean): Int {
    return listFiles()?.filter {
        if (countHiddenItems) {
            true
        } else {
            !it.name.startsWith('.')
        }
    }?.size ?: 0
}

fun File.newName(): String {
    var index = 1
    var candidate: String
    val split = nameWithoutExtension.split(' ').toMutableList()
    val last = split.last()
    if ("""^\(\d+\)$""".toRegex().matches(last)) {
        split.removeLast()
    }
    val name = split.joinToString(" ")
    while (true) {
        candidate = if (extension.isEmpty()) "$name ($index)" else "$name ($index).$extension"
        if (!File("$parent/$candidate").exists()) {
            return candidate
        }
        index++
    }
}

fun File.newPath(): String {
    return "$parent/" + newName()
}

fun File.getBitmap(context: Context, width: Int, height: Int, centerCrop: Boolean): Bitmap? {
    var bitmap: Bitmap? = null
    if (true) {
        try {
            bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(this, Size(width, height), null)
            } else {
                ThumbnailUtils.createVideoThumbnail(this.absolutePath, MediaStore.Video.Thumbnails.MICRO_KIND)
            }
        } catch (ex: Exception) {

        }
    } else {
        var options = RequestOptions().set(Downsampler.ALLOW_HARDWARE_CONFIG, true).override(width, height)
        if (centerCrop) {
            options = options.centerCrop()
        }
        val d = Glide.with(context).load(this)
            .apply(options)
            .submit().get()
        bitmap = (d as BitmapDrawable).bitmap
    }
    return bitmap
}

fun File.toThumbBytes(context: Context, width: Int, height: Int, centerCrop: Boolean): ByteArray {
    val stream = ByteArrayOutputStream()
    getBitmap(context, width, height, centerCrop)?.compress(Bitmap.CompressFormat.JPEG, 80, stream)
    return stream.toByteArray()
}

fun File.getDuration(context: Context): Long {

    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, Uri.fromFile(this))
    val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    retriever.release()
    return (time?.toLong()?.div(1000)) ?: 0L
}