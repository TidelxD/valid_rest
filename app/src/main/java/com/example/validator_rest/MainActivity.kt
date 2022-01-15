package com.example.validator_rest

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val root =
        Firebase.database("https://valid-rest-default-rtdb.europe-west1.firebasedatabase.app/")
    private val RestaurantRef = root.getReference("Restaurant")
    val REQUEST_CODE = 1
    private lateinit var zxingScannerView: ZXingScannerView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        zxingScannerView = findViewById(R.id.zxingScannerView)
        zxingScannerView.setResultHandler(this)
        mediaPlayer = MediaPlayer.create(this, R.raw.scanner_sound)

        checkpermission()
    }


    @SuppressLint("InflateParams")
    private fun openDialog(result: Int) {
        val builder = AlertDialog.Builder(this)
        val view =
            LayoutInflater.from(applicationContext).inflate(R.layout.dialog_add_credit, null, false)

        val addCreditImage: ImageView = view.findViewById(R.id.add_credit_image)
        val addCreditText: TextView = view.findViewById(R.id.add_credit_text)
        val dialog = builder.create()

        if (result == 1) {
            addCreditText.text = "valid"
            addCreditImage.setImageDrawable(getDrawable(R.drawable.ic_checked))

        } else {

            addCreditText.visibility = View.VISIBLE
            addCreditImage.visibility = View.VISIBLE
            addCreditText.text = "not valid! "
            addCreditImage.setImageDrawable(getDrawable(R.drawable.ic_error))
            zxingScannerView.startCamera()
            zxingScannerView.setResultHandler(this)
        }
        dialog.setView(view)
        dialog.setCancelable(true)

        dialog.show()
        val handler = Handler()
        val runnable = Runnable { dialog.dismiss() }
        handler.postDelayed(runnable, 1000)
        zxingScannerView.startCamera()
        zxingScannerView.setResultHandler(this)


    }

    override fun handleResult(rawResult: Result?) {
        mediaPlayer.start()
        val uid = rawResult.toString()
        RestaurantRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    openDialog(0)
                    return
                }

                openDialog(1)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    applicationContext,
                    "Error : ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    fun checkpermission() {

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE
            )
        } else {
            zxingScannerView.startCamera()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    zxingScannerView.startCamera()

                }
            }
        }

    }


    override fun onResume() {
        super.onResume()
        zxingScannerView.startCamera()
        zxingScannerView.setResultHandler(this)
    }

    override fun onPause() {
        super.onPause()
        zxingScannerView.stopCamera()
    }
}