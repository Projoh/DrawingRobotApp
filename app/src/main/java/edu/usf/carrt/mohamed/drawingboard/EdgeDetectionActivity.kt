package edu.usf.carrt.mohamed.drawingboard

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_edge_detection.*
import android.content.Intent
import android.graphics.Bitmap
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_drawing.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.io.File
import java.lang.Exception


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class EdgeDetectionActivity : AppCompatActivity() {
    var rgba : Mat? = null;


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> this.finish()
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edge_detection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        supportActionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
        // Set up the user interaction to manually show or hide the system UI.
//        fullscreen_image.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        dummy_button.setOnTouchListener(mDelayHideTouchListener)
        fullscreen_image.setImageBitmap(bitmap)

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
        createBorderImage(0.0)

        slider_element.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                val rate = progress * 5.0
                createBorderImage(rate)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        send_button.setOnClickListener {
            val imageToDraw = createImageToDrawFromArray(imageArray)
            var result = PointToRobotConverter(imageArray.size, imageArray[0].size).drawPicture(imageToDraw)

            val path = baseContext.filesDir
            val letDirectory = File(path, "LET")
            letDirectory.mkdirs()

            val file = File(letDirectory, "commands.dmc")
            file.writeText(result);

            SFTPClient.upload(baseContext, file)
//            val result = Intent()
//            result.putExtra("image",Gson().toJson(imageToDraw))
//            setResult(Activity.RESULT_OK, result);
            finish()
        }

    }

    var imageArray : Array<Array<Int>> = arrayOf<Array<Int>>()
    private fun createBorderImage(rate: Double) {
        Utils.bitmapToMat(bitmap, rgba)

        val edges = Mat(rgba!!.size(), CvType.CV_8UC1)
        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4)
        Imgproc.blur(edges, edges, Size(3.0,3.0))
        Imgproc.Canny(edges, edges, 50.0, rate)

        val rows = edges.rows() - 1

        imageArray = arrayOf<Array<Int>>()
        for (i in 0..rows) {
            val cols = edges.row(i).cols()
            var array = arrayOf<Int>()
            for(j in 0..cols) {
                val value = if (edges.get(j, i) != null) edges.get(j, i)[0].toInt() else 0
                array += value.toInt()
            }
            imageArray += array
        }
        val resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(edges, resultBitmap)

        fullscreen_image.setImageBitmap(resultBitmap)

    }

    private fun createImageToDrawFromArray(imageArray: Array<Array<Int>>) : ImageToDraw{
        // loop through every element in array
        // if positive, create line
        val imageToDraw = ImageToDraw()
        for (i in imageArray.indices) {
            for (j in imageArray[i].indices) {
                if(imageArray[i][j] > 0) {
                    val segment = Segment()
                    addChildrenToSegment(imageArray,i,j, segment)
                    imageToDraw.segments.add(segment)
                }
            }
        }
        return  imageToDraw
    }

    private fun addChildrenToSegment(imageArray: Array<Array<Int>>, i: Int, j: Int, segment: Segment) {
        val converter = PointToRobotConverter(imageArray.size, imageArray[0].size)
        val point = converter!!.convertToPolarBasedOffScreenSize(j.toFloat(), i.toFloat())
        val imageValAtPoint = imageArray[i][j]
        if(imageValAtPoint <= 0){
            return
        }

        segment.lines.add(point)
        imageArray[i][j] = -imageArray[i][j]
        if(i < imageArray.size-1) // down
            addChildrenToSegment(imageArray,i+1,j,segment)
        if(i > 0) // up
            addChildrenToSegment(imageArray,i-1,j,segment)
        if(j < imageArray[i].size-1) // right
            addChildrenToSegment(imageArray,i,j+1,segment)
        if(j > 0) // left
            addChildrenToSegment(imageArray,i,j-1,segment)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
    }



    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    rgba = Mat()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }



    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }



    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300

        private var bitmap : Bitmap? = null

        fun newIntent(context: Context, image: Bitmap): Intent {
            val intent = Intent(context, EdgeDetectionActivity::class.java)
            bitmap = image;
            return intent
        }

        init {
            if (!OpenCVLoader.initDebug()) {
                // Handle initialization error
            }
        }
    }
}
