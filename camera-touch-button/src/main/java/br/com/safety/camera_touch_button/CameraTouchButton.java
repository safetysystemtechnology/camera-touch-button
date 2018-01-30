package br.com.safety.camera_touch_button;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author NetoDevel
 */
public class CameraTouchButton extends RelativeLayout implements View.OnTouchListener {

    private static final String DEFAULT_TOUCH_MESSAGE = "Press and hold to take picture";

    private RelativeLayout mRootLayout;
    private Context mContext;
    private ProgressFrameLayout mProgressFrameLayout;
    private CameraView mCameraView;
    private CameraListener mCameraListener;

    private int circleWidth = 0;
    private int circleHeight = 0;
    private String touchMessage;

    private Timer timer;
    private boolean clicked = false;

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
        timer = new Timer();
        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CameraTouchButton, defStyleAttr, defStyleRes);
            circleWidth = (int) typedArray.getDimension(R.styleable.CameraTouchButton_camera_circle_width, ViewGroup.LayoutParams.WRAP_CONTENT);
            circleHeight = (int) typedArray.getDimension(R.styleable.CameraTouchButton_camera_circle_height, ViewGroup.LayoutParams.WRAP_CONTENT);
            touchMessage = (String) typedArray.getString(R.styleable.CameraTouchButton_touch_message);
        }

        this.setOnTouchListener(this);
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

            mCameraView.bindCameraKitListener(this);

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

        if (clicked == false) {
            mProgressFrameLayout.addView(mCameraView, cameraParams);
            clicked = true;
        }
    }

    public void hide() {
        this.mProgressFrameLayout.removeView(mCameraView);
        this.mProgressFrameLayout.setVisibility(INVISIBLE);
    }

    private void hideAndCaptureImage() {
        mCameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
            @Override
            public void callback(CameraKitImage event) {
                if (mCameraListener != null) {
                    mCameraListener.onCaptured(event);
                }
            }
        });

        mCameraView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (clicked) {
                    mProgressFrameLayout.setVisibility(INVISIBLE);
                    removeCameraView();
                }
            }
        }, 300);
    }

    private void removeCameraView() {
        this.mProgressFrameLayout.removeView(mCameraView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.mCameraView.setOutlineProvider(null);
        }

        clicked = false;
    }

    public CameraView getCamera() {
        return this.mCameraView;
    }
    private TimerTask pressTask;
    private Boolean pressedLongEnough;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startCountingPressedTime();
                return true;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                hideAndCaptureWhenPressedLongEnough();
                break;
            default:
                return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startCountingPressedTime() {
        pressedLongEnough = false;
        pressTask = new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        show();
                        pressTask = null;
                        pressedLongEnough = true;
                    }
                });
            }
        };
        timer.schedule(pressTask, 600);
    }

    private void hideAndCaptureWhenPressedLongEnough() {
        if (pressTask != null) pressTask.cancel();
        pressTask = null;
        if (pressedLongEnough)
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    hideAndCaptureImage();
                }
            });
        else
            Toast.makeText(getContext(), touchMessage != null ? touchMessage : DEFAULT_TOUCH_MESSAGE, Toast.LENGTH_LONG).show();
        pressedLongEnough = false;
    }

}