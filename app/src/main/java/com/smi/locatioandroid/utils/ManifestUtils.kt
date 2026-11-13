package com.smi.locatioandroid.utils

import android.content.Context
import android.content.pm.PackageManager

object ManifestUtils {
    // Función para recuperar la clave API del archivo de manifiesto
    fun  getApiKeyFromManifest (context: Context) : String? {
        return  try {
            val applicationInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val bundle = applicationInfo.metaData
            bundle.getString( "com.google.android.geo.API_KEY" )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }
}