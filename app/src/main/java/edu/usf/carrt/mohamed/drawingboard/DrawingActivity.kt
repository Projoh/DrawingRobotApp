package edu.usf.carrt.mohamed.drawingboard

import android.app.AppComponentFactory
import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import kotlinx.android.synthetic.main.activity_drawing.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class DrawingActivity : AppCompatActivity() {
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

        send_image_button.setOnClickListener {
            val drawingView = drawing_view

            var result = drawingView.getResult()

            val filename = "thefile.dmc"
            baseContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(result.toByteArray())
            }
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
