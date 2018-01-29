package br.com.safety.camera_touch_button;

import android.content.Context;

import com.wonderkiln.camerakit.CameraView;

import static com.wonderkiln.camerakit.CameraKit.Constants.METHOD_STANDARD;

/**
 * @author NetoDevel
 */
public class BuilderCameraView {

    private CameraView mCameraView;

    public CameraView build(Context context) {
        this.mCameraView = new CameraView(context);
        this.mCameraView.setMethod(METHOD_STANDARD);
        this.mCameraView.setCropOutput(false);
        this.mCameraView.setJpegQuality(70);
        return mCameraView;
    }

}
