package edu.usf.carrt.mohamed.drawingboard

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edu.usf.carrt.mohamed.drawingboard.SFTPClient.upload
import kotlinx.android.synthetic.main.activity_drawing.*
import org.opencv.android.OpenCVLoader
import java.io.File
import java.nio.file.Files.size

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.BaseLoaderCallback




/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class DrawingActivity : AppCompatActivity(), Drawer {
    override fun getCurrentPen(): Int {
        return if (selectedButton!!.id == 2131165249) 1 else if (selectedButton!!.id == 2131165303) 2 else 3
    }

    private val mHideHandler = Handler()
    private var selectedButton: ImageButton? = null
    val REQUEST_IMAGE_CAPTURE = 12;

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
            file.writeText(result);

            upload(baseContext, file)
        }


        delete_button.setOnClickListener {
            drawing_view.resetDrawing()
            delete_button.background = ColorDrawable(resources.getColor(R.color.red_800))

            Handler().postDelayed({
                delete_button.background = ColorDrawable(resources.getColor(R.color.amber_600))
            }, 200)
        }

        launch_camera.setOnClickListener{
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }


    }




    fun changeColor(view: View) {
        var imagebutton = view as ImageButton
        selectedButton = imagebutton
        first_button.background = ColorDrawable(resources.getColor(R.color.amber_600))
//        second_Button.background = ColorDrawable(resources.getColor(R.color.amber_600))
//        third_button.background = ColorDrawable(resources.getColor(R.color.amber_600))

        selectedButton!!.background = ColorDrawable(resources.getColor(R.color.amber_900))
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data!!.extras.get("data") as Bitmap
            image_from_camera.setImageBitmap(imageBitmap)
            detectEdges(imageBitmap)
        }
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK) {
//                var imageToDrawString = data!!.extras.get("image")  as String
//                var gson = Gson()
//                var imageToDraw = gson.fromJson(imageToDrawString, ImageToDraw::class.java)
//            var res = new.drawPicture(imageToDraw)
            }
        }
    }

    private fun detectEdges(bitmap: Bitmap) {

        val intent = EdgeDetectionActivity.newIntent(this,bitmap)
        var bundle = Bundle()
        startActivityForResult(intent, 1, bundle)
        val huh = "yeet"


//        Utils.bitmapToMat(bitmap, rgba)
//
//        val edges = Mat(rgba!!.size(), CvType.CV_8UC1)
//        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4)
//        Imgproc.Canny(edges, edges, 100.0, 300.0)
//
//        val columns = edges.cols()
//        val rows = edges.rows()
//
//        val test = edges.col(0);
//        val test2 = edges.row(0)
//        val testEdge = test[0,0]
//        val resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(edges, resultBitmap)
//
//        image_from_camera.setImageBitmap(resultBitmap)
    }

    public override fun onResume() {
        super.onResume()
//        if (!OpenCVLoader.initDebug()) {
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
//        } else {
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
//        }
    }



    companion object {
        init {
            if (!OpenCVLoader.initDebug()) {
                // Handle initialization error
            }
        }


    }
}

