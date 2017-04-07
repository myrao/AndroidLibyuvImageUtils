package tech.shutu.androidlibyuvimageutils.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tech.shutu.androidlibyuvimageutils.R;
import tech.shutu.androidlibyuvimageutils.utils.CameraHelper;
import tech.shutu.androidlibyuvimageutils.utils.FileUtil;
import tech.shutu.jni.YuvUtils;

import static tech.shutu.androidlibyuvimageutils.utils.CameraHelper.DST_HEIGHT;
import static tech.shutu.androidlibyuvimageutils.utils.CameraHelper.DST_WIDTH;

/**
 * Created by raomengyang on 31/03/2017.
 */
public class CameraPreviewActivity extends BaseActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public static int DISPLAY_DEGREE = 0;

    @BindView(R.id.sv_camera_preview)
    SurfaceView svCameraPreview;
    @BindView(R.id.btn_switch_camera)
    ImageButton btnSwitchCamera;
    @BindView(R.id.iv_preview)
    ImageView ivPreview;

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private boolean isFrontCamera = false;

    private ExecutorService executorService;


    public static void startActivity(Context ctx) {
        Intent it = new Intent(ctx, CameraPreviewActivity.class);
        ctx.startActivity(it);
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_camera_preview);
    }

    @Override
    public void initViews() {
        initSurfaceHolder();
    }

    private void initSurfaceHolder() {
        YuvUtils.allocateMemo(CameraHelper.PREVIEW_WIDTH * CameraHelper.PREVIEW_HEIGHT * 3 / 2, 0,
                CameraHelper.DST_WIDTH * CameraHelper.DST_HEIGHT * 3 / 2);
        mSurfaceHolder = svCameraPreview.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.setFixedSize(CameraHelper.PREVIEW_WIDTH, CameraHelper.PREVIEW_HEIGHT);
    }

    @Override
    public void initData() {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isFrontCamera) {
            openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        saveToStorage(data);
        mCamera.addCallbackBuffer(data);
    }

    private void saveToStorage(final byte[] data) {
        if (executorService == null) executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                byte[] dstYuv = new byte[DST_WIDTH * DST_HEIGHT * 3 / 2];
                YuvUtils.scaleAndRotateYV12ToI420(data, dstYuv,
                        CameraHelper.PREVIEW_WIDTH, CameraHelper.PREVIEW_HEIGHT, 90, DST_WIDTH, CameraHelper.DST_HEIGHT);
                if (dstYuv != null && dstYuv.length > 0) {
                    FileUtil.saveYuvToSdCardStorage(dstYuv); // save yuv bytes to sdcard
                }
            }
        });
    }

    @OnClick(R.id.btn_switch_camera)
    public void onClick() {
        changeCamera();
    }


    private void openCamera(int cameraId) {
        releaseCamera();
        if (null == createCamera(cameraId)) {
            throw new NullPointerException("camera can not open");
        }
        if (CameraHelper.configCamera(mCamera)) {
            setCameraDisplayOrientation(this, cameraId, mCamera);
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mCamera.setPreviewCallback(this);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    private Camera createCamera(int cameraId) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mCamera;
    }

    private synchronized void releaseCamera() {
        if (null != mCamera) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void changeCamera() {
        isFrontCamera = !isFrontCamera;
        openCamera((isFrontCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK));
    }

    // 设置相机屏幕显示方向（注意仅仅是屏幕的显示方向改变，摄像头采集的图像方向并未改变）
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            DISPLAY_DEGREE = (info.orientation + degrees) % 360;
            DISPLAY_DEGREE = (360 - DISPLAY_DEGREE) % 360;  // compensate the mirror
        } else {
            // back-facing
            DISPLAY_DEGREE = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(DISPLAY_DEGREE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YuvUtils.releaseMemo();
    }
}
