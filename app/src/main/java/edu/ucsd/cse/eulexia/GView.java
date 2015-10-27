package edu.ucsd.cse.eulexia;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

/**
 * Created by phoebe on 10/20/15.
 */
public class GView extends JavaCameraView {
    public GView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public GView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean initializeCamera(int width, int height) {
        super.initializeCamera(width, height);

        Camera.Parameters params = mCamera.getParameters();

        params.setPreviewFpsRange(30000, 30000);
        mCamera.setParameters(params);

        return true;
    }
}
