package s.yarlykov.example.extentions

import android.util.Log

private const val LOG_TAG = "APP_TAG"

fun logIt(message: String, tag: String = LOG_TAG) = Log.d(tag, message)
