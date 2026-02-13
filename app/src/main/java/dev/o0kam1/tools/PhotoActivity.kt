package dev.o0kam1.tools

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import io.getstream.photoview.PhotoView
import io.github.duzhaokun123.yabr.R
import io.github.duzhaokun123.yabr.logger.activityLogger
import io.github.duzhaokun123.yabr.module.core.ModuleActivity
import io.github.duzhaokun123.yabr.module.core.ModuleActivityMeta
import io.github.duzhaokun123.yabr.module.core.Share
import io.github.duzhaokun123.yabr.utils.Http
import io.github.duzhaokun123.yabr.utils.Toast
import io.github.duzhaokun123.yabr.utils.readAll
import io.github.duzhaokun123.yabr.utils.reason
import io.github.duzhaokun123.yabr.utils.runNewThread

@ModuleActivity
class PhotoActivity : Activity(), ModuleActivityMeta {
    companion object {
        const val URL = "url"
        const val TITLE = "title"
    }

    val logger = activityLogger

    var photoData = byteArrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_photo)
        setTitle(intent.getStringExtra(TITLE) ?: "图片")
        val url = intent.getStringExtra(URL)
        if (url == null) {
            Toast.show("url is null")
            finish()
            return
        }
        val photoView = findViewById<PhotoView>(R.id.photo_view)
        runNewThread {
            runCatching {
                photoData = Http.get(url).readAll()
                val b = BitmapFactory.decodeByteArray(photoData, 0, photoData.size)
                runOnUiThread {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && b.hasGainmap()) {
                        window.colorMode = ActivityInfo.COLOR_MODE_HDR
                    }
                    photoView.setImageBitmap(b)
                    findViewById<ProgressBar>(R.id.pb).visibility = View.GONE
                }
            }.onFailure { t ->
                logger.e("Failed to load cover image")
                logger.e(t)
                Toast.show("图片加载失败: ${t.reason}")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_photo, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.share -> {
                val shareUri = Share.makeShareFileUri(photoData, "jpeg")
                startActivity(Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setDataAndType(shareUri, contentResolver.getType(shareUri))
                }, "分享图片"))
                true
            }

            R.id.save -> {
                runNewThread {
                    val file =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            ?.resolve("BiliBili")
                            ?.resolve("${System.currentTimeMillis()}.jpeg") // 可能不是 jpeg 猜的
                    if (file == null) {
                        Toast.show("无法获取存储目录")
                    } else {
                        runCatching {
                            file.parentFile?.mkdirs()
                            file.writeBytes(photoData)
                            Toast.show("图片已保存到: ${file.absolutePath}")
                        }.onFailure { t ->
                            logger.e("Failed to save cover image")
                            logger.e(t)
                            Toast.show("图片保存失败: ${t.reason}")
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}