package br.com.safety.camera_touch_button;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

/**
 * @author NetoDevel
 */
public class CameraTouchButton extends RelativeLayout {

    private RelativeLayout mRootLayout;
    private Context mContext;
    private ProgressFrameLayout mProgressFrameLayout;
    private CameraView mCameraView;
    private CameraListener mCameraListener;

    private int circleWidth = 0;
    private int circleHeight = 0;

    public CameraTouchButton(Context context) {
        super(context);
        init(context, null, -1, -1);
    }

    public CameraTouchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, -1);
    }

    public CameraTouchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, -1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CameraTouchButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setCameraListener(CameraListener cameraListener) {
        mCameraListener = cameraListener;
    }

    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CameraTouchButton, defStyleAttr, defStyleRes);
            circleWidth = (int) typedArray.getDimension(R.styleable.CameraTouchButton_camera_circle_width, ViewGroup.LayoutParams.WRAP_CONTENT);
            circleHeight = (int) typedArray.getDimension(R.styleable.CameraTouchButton_camera_circle_height, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                show();
                return true;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                hideAndCaptureImage();
                break;
            default:
                return false;
        }
        return true;
    }

    public void setup(RelativeLayout rootLayout, CameraView cameraView) {
        this.mRootLayout = rootLayout;
        this.mCameraView = cameraView;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void show() {
        if (mProgressFrameLayout == null) {
            mProgressFrameLayout = new ProgressFrameLayout(mContext);
            mProgressFrameLayout.setVisibility(VISIBLE);
            mProgressFrameLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_circle));

            LayoutParams layoutParams = new LayoutParams(circleWidth != 0 ? circleWidth : 290, circleHeight != 0 ? circleHeight : 290);
            layoutParams.addRule(CENTER_IN_PARENT, TRUE);

            mProgressFrameLayout.startWithOutProgress();

            /**
             * Animation
             */
            Animation animFadeIn = AnimationUtils.loadAnimation(this.mContext, R.anim.to_up);
            animFadeIn.reset();

            mProgressFrameLayout.clearAnimation();
            mProgressFrameLayout.startAnimation(animFadeIn);

            /**
             * CameraView
             */
            addCameraView();

            /**
             * Crop Views
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cropViews();
            }

            /**
             * Add ProgressFrameLayout to root
             */
            mRootLayout.addView(mProgressFrameLayout, layoutParams);

            this.mCameraView.addCameraKitListener(new CameraKitEventListener() {
                @Override
                public void onEvent(CameraKitEvent cameraKitEvent) {

                }

                @Override
                public void onError(CameraKitError cameraKitError) {

                }

                @Override
                public void onImage(CameraKitImage cameraKitImage) {
                    if (mCameraListener != null) {
                        mCameraListener.onCaptured(cameraKitImage);
                    }
                }

                @Override
                public void onVideo(CameraKitVideo cameraKitVideo) {

                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cropViews();
            }
            addCameraView();
            this.mProgressFrameLayout.setVisibility(VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void cropViews() {
        ViewOutlineProvider vop = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(5, 5, view.getWidth() - 5, view.getHeight() - 5);
            }
        };

        this.mProgressFrameLayout.setOutlineProvider(vop);
        this.mProgressFrameLayout.setClipToOutline(true);

        ViewOutlineProvider vop2 = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(10, 10, mProgressFrameLayout.getWidth() - 10, mProgressFrameLayout.getHeight() - 10);
            }
        };

        mCameraView.setOutlineProvider(vop2);
        mCameraView.setClipToOutline(true);
    }

    private void addCameraView() {
        LayoutParams cameraParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        cameraParams.addRule(CENTER_IN_PARENT, TRUE);

        mProgressFrameLayout.addView(mCameraView, cameraParams);
    }

    public void hide() {
        this.mProgressFrameLayout.removeView(mCameraView);
        this.mProgressFrameLayout.setVisibility(INVISIBLE);
    }

    private void hideAndCaptureImage() {
        this.mCameraView.captureImage();
        this.mProgressFrameLayout.setVisibility(INVISIBLE);

        removeCameraView();
    }

    private void removeCameraView() {
        this.mProgressFrameLayout.removeView(mCameraView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mCameraView.setOutlineProvider(null);
        }
    }

    public CameraView getCamera() {
        return this.mCameraView;
    }

}