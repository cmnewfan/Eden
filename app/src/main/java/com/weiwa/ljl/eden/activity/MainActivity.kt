package com.weiwa.ljl.eden.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.Toast
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import com.weiwa.ljl.eden.Eden
import com.weiwa.ljl.eden.R
import com.weiwa.ljl.eden.network.ImaggaAuthentication
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.json.JSONArray
import org.json.JSONObject
import uk.co.senab.photoview.PhotoViewAttacher
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val pic = ByteArray(8194)
    private var imageUri: Uri? = null
    private var attacher: PhotoViewAttacher? = null
    private var fade_out_animation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (!Eden.CacheCategory.exists()) {
            Eden.CacheCategory.mkdir()
        }
        attacher = PhotoViewAttacher(mainImage)
        gallery.setOnClickListener(this)
        share.setOnClickListener(this)
        fade_out_animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out)
        fade_out_animation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                mainImage.setImageURI(null)
                mainImage.setImageURI(imageUri)
                attacher!!.update()
                mainImage.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        avi.hide()
        val intent: Intent
        when (v.id) {
            R.id.gallery -> {
                // 设定action和miniType
                intent = Intent()
                intent.action = Intent.ACTION_PICK
                intent.type = "image/*"
                intent.putExtra("return-data", true)
                // 以需要返回值的模式开启一个Activity
                startActivityForResult(intent, Activity.RESULT_FIRST_USER)
            }
            R.id.share -> if (imageUri != null) {
                val sendIntent = Intent(Intent.ACTION_SEND)
                //set intent type
                sendIntent.type = "image/*"
                sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "分享")
                sendIntent.setDataAndType(imageUri, "image/*")
                sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(Intent.createChooser(sendIntent, "分享"))
            } else {
                Toast.makeText(this@MainActivity, "Nothing Here", Toast.LENGTH_SHORT).show()
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val uri: Uri
        if (data != null && data.data != null) {
            Toast.makeText(this@MainActivity, "Target Captured\nNoblesse Oblige", Toast.LENGTH_SHORT).show()
            uri = data.data
            imageUri = uri
            try {
                avi.show()
                mainImage.setImageBitmap(MediaStore.Images.Media.getBitmap(contentResolver, uri))
                attacher!!.update()
                val file = File(Eden.CacheCategory, "temp.png")
                if (!file.exists()) {
                    file.createNewFile()
                }
                var bytesRead: Int
                val fis = contentResolver.openInputStream(uri)
                val fos = FileOutputStream(file)
                while (true) {
                    bytesRead = fis!!.read(pic, 0, pic.size)
                    if (bytesRead == -1) {
                        break
                    }
                    fos.write(pic, 0, bytesRead)
                }
                fos.close()
                fis.close()
                postPic(file)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (resultCode == Activity.RESULT_OK) {
            avi.show()
            mainImage.setImageURI(null)
            mainImage.setImageURI(Uri.fromFile(File(Eden.CacheCategory, "Pic.jpg")))
            attacher!!.update()
            Toast.makeText(this@MainActivity, "Target Captured\nNoblesse Oblige", Toast.LENGTH_SHORT).show()
            postPic(File(Eden.CacheCategory, "Pic.jpg"))
        } else {
            Toast.makeText(this@MainActivity, "Target Missing\nNoblesse Oblige", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun postPic(pic: File) {
        Toast.makeText(this@MainActivity, "Target Uploading\nNoblesse Oblige", Toast.LENGTH_SHORT).show()
        async(CommonPool) {
            var response: HttpResponse<*>? = null
            try {
                response = Unirest.post("http://api.imagga.com/v1/content")
                        .basicAuth(ImaggaAuthentication.api_key, ImaggaAuthentication.api_secret)
                        .field("image", pic)
                        .asJson()
            } catch (e: UnirestException) {
                runOnUiThread {
                    avi.hide()
                    Toast.makeText(this@MainActivity, e.message + "\nNoblesse Oblige", Toast.LENGTH_SHORT).show()
                }
            }
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Target Analyzing\nNoblesse Oblige", Toast.LENGTH_SHORT).show()
            }
            try {
                val tag_id = JSONObject(response!!.body.toString()).getJSONArray("uploaded").getJSONObject(0).getString("id")
                response = Unirest.get("http://api.imagga.com/v1/tagging")
                        .field("content", tag_id)
                        .field("language", "zh_chs")
                        .basicAuth(ImaggaAuthentication.api_key, ImaggaAuthentication.api_secret)
                        .asJson()
                val `object` = JSONObject(response!!.body.toString()).getJSONArray("results").getJSONObject(0)
                val tags = `object`.getJSONArray("tags")
                if (tags != null) {
                    val result = TagsFilter(tags, 20)
                    if (result == null || result.length() == 0) {
                        runOnUiThread {
                            avi.hide()
                            Toast.makeText(this@MainActivity, "Target has no tags\nNoblesse Oblige", Toast.LENGTH_SHORT).show()
                        }
                    } else if (imageUri != null) {
                        imageUri = drawTags(imageUri, result)
                        runOnUiThread {
                            avi.hide()
                            mainImage.startAnimation(fade_out_animation)
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Target Missing\nNoblesse Oblige", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    avi.hide()
                    Toast.makeText(this@MainActivity, e.message + "\nNoblesse Oblige", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun TagsFilter(tags: JSONArray, filter: Int): JSONArray {
        var jsList: ArrayList<JSONObject> = ArrayList(tags.length())
        for (i in 0..tags.length() - 1) {
            jsList.add(tags.getJSONObject(i))
        }
        val list = jsList.filter { it.getInt("confidence") >= filter }
        return JSONArray(list.subList(0, if (list.size >= 5) 5 else list.size))
    }

    private fun drawTags(coverUri: Uri?, content: JSONArray): Uri? {
        val mPaint = Paint()
        mPaint.color = Color.WHITE
        mPaint.textAlign = Paint.Align.LEFT
        mPaint.textSize = 25f
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
        var bitmap: Bitmap? = null
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, coverUri).copy(Bitmap.Config.ARGB_8888, true)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val tempBitmap = Bitmap.createBitmap(bitmap!!.width, bitmap.height + 200, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(tempBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawRect(0f, bitmap.height.toFloat(), bitmap.width.toFloat(), (bitmap.height + 200).toFloat(), mPaint)
        mPaint.color = Color.BLACK
        var lines = 1
        for (i in 0..content.length() - 1) {
            if (bitmap.width < mPaint.measureText(content.getJSONObject(i).getString("tag") + ":" + content.getJSONObject(i).getString("confidence"))) {
                val textLines = getTextList(bitmap.width, content.getJSONObject(i).getString("tag") + ":" + content.getJSONObject(i).getString("confidence"), mPaint.measureText(content.getJSONObject(i).getString("tag") + ":" + content.getJSONObject(i).getString("confidence")))
                for (j in textLines) {
                    canvas.drawText(j, 10f, (bitmap.height + lines * 25).toFloat(), mPaint)
                    lines++
                }
            } else {
                canvas.drawText(content.getJSONObject(i).getString("tag") + ":" + content.getJSONObject(i).getString("confidence"), 10f, (bitmap.height + lines * 25).toFloat(), mPaint)
                lines++
            }
        }
        return getUriOfBitmap(tempBitmap)
    }

    private fun getTextList(screen_width: Int, text: String, textWidth: Float): ArrayList<String> {
        val length = text.length
        var startIndex = 0
        var endIndex = Math.min((length.toFloat() * (screen_width / textWidth)).toInt(), length)
        val perLineLength = endIndex - startIndex
        val lines = ArrayList<String>()
        lines.add(text.subSequence(startIndex, endIndex).toString())
        while (endIndex < length) {
            startIndex = endIndex
            endIndex = Math.min(startIndex + perLineLength, length)
            lines.add(text.subSequence(startIndex, endIndex).toString())
        }
        return lines
    }

    private fun getUriOfBitmap(tempBitmap: Bitmap): Uri? {
        val png_file = File(Eden.CacheCategory.toString() + "/output.png")
        if (png_file.exists()) {
            png_file.delete()
        }
        try {
            png_file.createNewFile()
            val fos = FileOutputStream(png_file)
            tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            return Uri.fromFile(png_file)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}
