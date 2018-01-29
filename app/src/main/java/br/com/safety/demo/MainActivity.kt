package br.com.safety.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import br.com.safety.camera_touch_button.BuilderCameraView
import com.wonderkiln.camerakit.CameraView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /**
         * Build CameraView
         */
        cameraView = BuilderCameraView().build(this)

        /**
         * Setup camera touch button
         */
        camera_touch_button.setup(root_layout, cameraView)

        /**
         * Listener callback photo file
         */
        camera_touch_button.setCameraListener {
            Toast.makeText(this, "Captured", Toast.LENGTH_SHORT).show()
            Log.d("MainAcitivity", "your_jpeg" + it.jpeg);
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onStop() {
        super.onStop()
        cameraView.stop()
    }

}
