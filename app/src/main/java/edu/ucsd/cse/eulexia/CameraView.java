package edu.ucsd.cse.eulexia;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by andrewdu on 11/18/15.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback
{
    private SurfaceHolder surfaceHolder = null;
    private Camera camera = null;

    @SuppressWarnings("deprecation")
    public CameraView(Context context)
    {
        super(context);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        camera = Camera.open();

        // Set the Hotfix for Google Glass
        this.setCameraParameters(camera);

        // Show the Camera display
        try
        {
            camera.setPreviewDisplay(holder);
        }
        catch (Exception e)
        {
            this.releaseCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        // Start the preview for surfaceChanged
        if (camera != null)
        {
            camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // Do not hold the camera during surfaceDestroyed - view should be gone
        this.releaseCamera();
    }

    public void setCameraParameters(Camera camera)
    {
        if (camera != null)
        {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewFpsRange(30000, 30000);
            camera.setParameters(parameters);
        }
    }

    public void releaseCamera()
    {
        if (camera != null)
        {
            camera.release();
            camera = null;
        }
    }
}
