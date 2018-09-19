package edu.usf.carrt.mohamed.drawingboard

import android.app.AppComponentFactory
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import edu.usf.carrt.mohamed.drawingboard.SFTPClient.upload
import kotlinx.android.synthetic.main.activity_drawing.*
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class DrawingActivity : AppCompatActivity(), Drawer {
    override fun getCurrentPen(): Int {
        return if (selectedButton!!.id == 2131165249) 1 else if (selectedButton!!.id == 2131165303) 2 else 3
    }

    private val mHideHandler = Handler()
    private var selectedButton : ImageButton ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_drawing)
        // Set up the user interaction to manually show or hide the system UI.
        selectedButton = first_button
        first_button.background = ColorDrawable(resources.getColor(R.color.amber_900))

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        dummy_button.setOnTouchListener(mDelayHideTouchListener)

        drawing_view.linkDrawer(this)

        send_image_button.setOnClickListener {
            val drawingView = drawing_view

            var result = drawingView.getResult()

            val path = baseContext.filesDir
            val letDirectory = File(path, "LET")
            letDirectory.mkdirs()

            val file = File(letDirectory, "commands.dmc")
            file.appendText(result);

//            val scpSend = SCPSend()
//            val connected = scpSend.sendFile("pi","carrt123","192.168.4.1", file)
            upload(file)
           }


        delete_button.setOnClickListener {
            drawing_view.resetDrawing()
            delete_button.background = ColorDrawable(resources.getColor(R.color.red_800))

            Handler().postDelayed({
                delete_button.background = ColorDrawable(resources.getColor(R.color.amber_600))
            }, 200)
        }



    }

    fun changeColor(view : View) {
        var imagebutton = view as ImageButton
        selectedButton = imagebutton
        first_button.background = ColorDrawable(resources.getColor(R.color.amber_600))
        second_Button.background = ColorDrawable(resources.getColor(R.color.amber_600))
        third_button.background = ColorDrawable(resources.getColor(R.color.amber_600))

        selectedButton!!.background = ColorDrawable(resources.getColor(R.color.amber_900))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

    }

}
