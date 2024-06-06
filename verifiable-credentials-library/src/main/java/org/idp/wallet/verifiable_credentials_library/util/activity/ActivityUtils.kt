package org.idp.wallet.verifiable_credentials_library.util.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

fun move(context: Context, url: String) {
  try {
    println(url)
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    context.startActivity(intent)
  } catch (e: Exception) {
    Log.e("VC library", e.message ?: "failed move app with deepLink")
  }
}
