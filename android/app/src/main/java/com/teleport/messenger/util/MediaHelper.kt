package com.teleport.messenger.util

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import java.io.File

object MediaHelper {
    fun copyUriToCache(context: Context, uri: Uri, suffix: String): File? = runCatching {
        val out = File(context.cacheDir, "upload_${System.currentTimeMillis()}$suffix")
        context.contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { output -> input.copyTo(output) }
        }
        out
    }.getOrNull()
}

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var file: File? = null
    private var startedAt = 0L

    fun start(): File? = runCatching {
        file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file!!.absolutePath)
            prepare()
            start()
        }
        startedAt = System.currentTimeMillis()
        file
    }.getOrNull()

    fun stop(): Pair<File, Long>? {
        return runCatching {
            recorder?.stop()
            recorder?.release()
            recorder = null
            val f = file ?: return null
            val duration = System.currentTimeMillis() - startedAt
            f to duration
        }.getOrNull()
    }

    fun cancel() {
        runCatching { recorder?.stop() }
        recorder?.release()
        recorder = null
        file?.delete()
        file = null
    }
}
