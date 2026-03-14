package com.example.drawingapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.createBitmap
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    val openGalleryLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val imageBackground = findViewById<ImageView>(R.id.iv_background)
            imageBackground.setImageURI(result.data?.data)
        }
    }
    private val cameraResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

            if (isGranted) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
                val pickIntent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)

            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }

        }


    private var drawingView: DrawingView? = null
    private var brushButton: ImageButton? = null
    private var mImageButtonCurrentPaint: ImageButton? = null

    private var dialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val linearLayoutColors = findViewById<LinearLayout>(R.id.ll_paint_colors)
        mImageButtonCurrentPaint = linearLayoutColors[1] as ImageButton
        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSize(10.toFloat())
        brushButton = findViewById(R.id.ib_brush)
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_selectedl)
        )
        brushButton!!.setOnClickListener {
            showBrushSizeChooseDialog()
        }
        val saveButton: ImageButton = findViewById(R.id.save_image)
        val buttonCamera: ImageButton = findViewById(R.id.gallery)
        buttonCamera.setOnClickListener {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                Toast.makeText(
                    this@MainActivity,
                    "Allow from settings",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    cameraResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        }
        saveButton.setOnClickListener {
            if (isPermissionAllowed()) {
                val frameView: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                showRationalDialog()
                lifecycleScope.launch {
                    saveBitmapFile(getBitmapFromView(frameView))
                }
            }
        }

    }

    private fun isPermissionAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
        return result == PackageManager.PERMISSION_GRANTED

    }

    private fun showBrushSizeChooseDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size: ")

        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        smallBtn.setOnClickListener {
            drawingView?.setSize(10.toFloat())
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener {
            drawingView?.setSize(20.toFloat())
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener {
            drawingView?.setSize(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if (view !== mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_selectedl
                )
            )
            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_normal
                )
            )
            mImageButtonCurrentPaint = view

        }
    }

    private fun showRationalDialog() {
        dialog = Dialog(this)
        dialog!!.setContentView(R.layout.dialog_custom_progress)
        dialog!!.show()

    }

    private fun closeDialog(){
        dialog!!.dismiss()
        dialog=null;
    }

    fun undo(view: View) {
        drawingView!!.undo();
    }

    fun redo(view: View) {
        drawingView!!.redo()
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val returnedBitMap = createBitmap(view.width, view.height)
        val canvas = Canvas(returnedBitMap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitMap
    }

    private suspend fun saveBitmapFile(mBitmao: Bitmap?): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (mBitmao != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmao.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val f = File(
                        externalCacheDir?.absoluteFile.toString()
                                + File.separator + "kidsDrawingApp_" + System.currentTimeMillis() / 1000 + ".png"
                    )
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                    runOnUiThread {
                        if (result.isNotEmpty()) {

                            Toast.makeText(
                                this@MainActivity,
                                "File saved ${result}",
                                Toast.LENGTH_LONG
                            ).show()
                            shareImage(result)
                            closeDialog()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something Went Wrong",
                                Toast.LENGTH_LONG
                            ).show()
                            closeDialog()
                        }
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                   runOnUiThread {
                       closeDialog()
                   }

                }
            }
        }

        return result
    }
    private fun shareImage(result: String){
        MediaScannerConnection.scanFile(this,arrayOf(result),null){
            path,uri->
            val shareIntent= Intent()
            shareIntent.action= Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
            shareIntent.type="Image/png"
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }
    }
}