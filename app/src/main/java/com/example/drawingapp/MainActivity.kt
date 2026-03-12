package com.example.drawingapp


import android.Manifest
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private val cameraResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

            if (isGranted) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()

            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }

        }
    private val cameraMultipleResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->

           isGranted.forEach {
               if(it.key== Manifest.permission.ACCESS_FINE_LOCATION){

               }
           }

        }
    private var drawingView: DrawingView? = null
    private var brushButton: ImageButton? = null
    private var mImageButtonCurrentPaint: ImageButton? = null


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
        val buttonCamera: ImageButton = findViewById(R.id.gallery)
        buttonCamera.setOnClickListener {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showRationalDialog(
                    "Permission demo requires camera access",
                    "Camera cannot be used because camera access is denied"
                )
            } else {
                cameraResultLauncher.launch(Manifest.permission.CAMERA)
            }
        }

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

    private fun showRationalDialog(
        title: String,
        message: String
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(message).setPositiveButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}