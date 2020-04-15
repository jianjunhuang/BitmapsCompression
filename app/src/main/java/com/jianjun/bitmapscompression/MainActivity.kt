package com.jianjun.bitmapscompression

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jianjun.bitmapscompression.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val displayFormat = "%s: \nwidth: %d height: %d\nsize: %d"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val originBmp = BitmapFactory.decodeStream(resources.openRawResource(R.raw.cat))
        binding.ivOrigin.setImageBitmap(originBmp)
        binding.tvOrigin.text = String.format(
            displayFormat,
            "Origin",
            originBmp.width,
            originBmp.height,
            originBmp.allocationByteCount / 1024
        )
        binding.compress.setOnClickListener {
            compress(originBmp)
        }
    }

    private fun compress(bmp: Bitmap) {
        binding.ivCompressed.post {
            val compressedBmp =
                decodeBitmapFromBitmap(bmp, binding.ivCompressed.width, binding.ivCompressed.height)
            binding.ivCompressed.setImageBitmap(compressedBmp)
            binding.tvCompressed.text = String.format(
                displayFormat,
                "Compressed",
                compressedBmp?.width,
                compressedBmp?.height,
                compressedBmp?.allocationByteCount?.div(1024)
            )
        }
    }

    private fun getConfig(): Bitmap.Config = when (binding.rgConfig.checkedRadioButtonId) {
        R.id.argb_8888 -> Bitmap.Config.ARGB_8888
        R.id.rgb_565 -> Bitmap.Config.RGB_565
        else -> Bitmap.Config.ARGB_8888
    }

    private fun getInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int =
        when (binding.rgSampleSize.checkedRadioButtonId) {
            R.id.rb_2 -> 2
            R.id.rb_4 -> 4
            R.id.rb_6 -> 6
            else -> calculateInSampleSize(options, reqWidth, reqHeight)
        }

    private fun scale(bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap? {
        val matrix = Matrix()
        matrix.setScale(reqWidth.toFloat() / bitmap.width, reqHeight.toFloat() / bitmap.height)
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            false
        )
    }

    private fun decodeBitmapFromBitmap(bitmap: Bitmap, reqWidth: Int, reqHeight: Int): Bitmap? {
        var result: Bitmap = bitmap
        if (binding.matrix.isChecked) {
            scale(bitmap, reqWidth, reqHeight)?.let {
                result = it
            }
        }

        val options = BitmapFactory.Options()
        val outPutStream = ByteArrayOutputStream()
        result.compress(Bitmap.CompressFormat.JPEG, 100, outPutStream)
        val array = outPutStream.toByteArray()
        options.inJustDecodeBounds = true
        options.inPreferredConfig = getConfig()
        BitmapFactory.decodeByteArray(array, 0, array.size, options)
        if (binding.inSampleSize.isChecked) {
            options.inSampleSize = getInSampleSize(options, reqWidth, reqHeight)
        }
        if (binding.inTargetDensity.isChecked) {
            options.inTargetDensity = resources.displayMetrics.densityDpi
        }

        options.inJustDecodeBounds = false
        result = BitmapFactory.decodeByteArray(array, 0, array.size, options)
        return if (binding.isScale.isChecked) scale(result, reqWidth, reqHeight) else result
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int, reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio =
                (height.toFloat() / reqHeight.toFloat()).roundToInt()
            val widthRatio =
                (width.toFloat() / reqWidth.toFloat()).roundToInt()
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }
}
