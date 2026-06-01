package com.example.pagaapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun bitmapDescriptorFromResource(context: Context, resId: Int, width: Int, height: Int): BitmapDescriptor {
    val bitmap = BitmapFactory.decodeResource(context.resources, resId)
    return if (bitmap != null) {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
        BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    } else {
        BitmapDescriptorFactory.defaultMarker()
    }
}
