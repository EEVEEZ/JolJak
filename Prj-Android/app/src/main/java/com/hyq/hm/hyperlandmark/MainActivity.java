package com.hyq.hm.hyperlandmark;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import zeusees.tracking.Face;
import zeusees.tracking.FaceTracking;

//import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    public void copyFilesFromAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                // directory
                File file = new File(newPath);
                if (!file.mkdir())
                {
                    Log.d("mkdir","can't make folder");

                }

                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, oldPath + "/" + fileName,
                            newPath + "/" + fileName);
                }
            } else {
                // file
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void InitModelFiles()
    {

        String assetPath = "ZeuseesFaceTracking";
        String sdcardPath = Environment.getExternalStorageDirectory()
                + File.separator + assetPath;
        copyFilesFromAssets(this, assetPath, sdcardPath);

    }


    private String[] denied;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.INTERNET};

    private FaceTracking mMultiTrack106 = null;
    private boolean mTrack106 = false;
    private int count = 2;
    private TextView textView;
    private boolean touched;
    private boolean countdown;

    CountDownTimer countDownTimer = new CountDownTimer(2500,1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            countdown = false;
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.valueOf(count));
            count--;
        }

        @Override
        public void onFinish() {
            count = 2;
            textView.setVisibility(View.INVISIBLE);
            touched = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.CountTextView);
        textView.setVisibility(View.INVISIBLE);
        textView.setTextColor(Color.parseColor("#FF0000"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (PermissionChecker.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    list.add(permissions[i]);
                }
            }
            if (list.size() != 0) {
                denied = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    denied[i] = list.get(i);
                }
                ActivityCompat.requestPermissions(this, denied, 5);
            } else {
                init();
            }
        } else {
            init();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 5) {
            boolean isDenied = false;
            for (int i = 0; i < denied.length; i++) {
                String permission = denied[i];
                for (int j = 0; j < permissions.length; j++) {
                    if (permissions[j].equals(permission)) {
                        if (grantResults[j] != PackageManager.PERMISSION_GRANTED) {
                            isDenied = true;
                            break;
                        }
                    }
                }
            }
            if (isDenied) {
                Toast.makeText(this, "请开启权限", Toast.LENGTH_SHORT).show();
            } else {
                init();

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private byte[] mNv21Data;
    private CameraOverlap cameraOverlap;
    private final Object lockObj = new Object();

    private SurfaceView mSurfaceView;

    private EGLUtils mEglUtils;
    private GLFramebuffer mFramebuffer;
    private GLFrame mFrame;
    private GLPoints mPoints;
    private GLBitmap mBitmap;
    private jjWebsocket mSocket;
    private int getCount = 0;

//    private boolean allowedCapture;

    private Button CaptureButton;

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast toast = Toast.makeText(getApplicationContext(),"Caputured",Toast.LENGTH_LONG);
            toast.show();
//            allowedCapture = true;
            touched = true;
            countdown = true;
        }
    };

    private void init(){
        InitModelFiles();

        CaptureButton = findViewById(R.id.Capture);
        CaptureButton.setOnClickListener(listener);

        mMultiTrack106 = new FaceTracking("/sdcard/ZeuseesFaceTracking/models");

        cameraOverlap = new CameraOverlap(this);
        mNv21Data = new byte[CameraOverlap.PREVIEW_WIDTH * CameraOverlap.PREVIEW_HEIGHT * 2];
        mFramebuffer = new GLFramebuffer();
        mFrame = new GLFrame();
        mPoints = new GLPoints();
        mBitmap = new GLBitmap(this,R.drawable.ic_logo);
        mSocket = new jjWebsocket();

        mSocket.init();
        mHandlerThread = new HandlerThread("DrawFacePointsThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        cameraOverlap.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                synchronized (lockObj) {
                    System.arraycopy(data, 0, mNv21Data, 0, data.length);
                }

                Bitmap bitmap = null;

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEglUtils == null) {
                            return;
                        }
                        mFrame.setS((float) 1.0);
//                        System.out.printf("\nSetS : %f\n", seekBarA.getProgress()/100.0f);
                        mFrame.setH((float) 0.0);
//                        System.out.printf("\nSetH : %f\n",seekBarB.getProgress()/360.0f);
                        mFrame.setL((float) 0.0);
//                        System.out.printf("\nSetL : %f\n", seekBarC.getProgress()/100.0f - 1);

                        if (mTrack106) {
                            mMultiTrack106.FaceTrackingInit(mNv21Data, CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
                            mTrack106 = !mTrack106;
                        } else {
                            mMultiTrack106.Update(mNv21Data, CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
                        }
                        boolean rotate270 = cameraOverlap.getOrientation() == 270;

                        List<Face> faceActions = mMultiTrack106.getTrackingInfo();
                        float[] p = null;
                        float[] points = null;
//                        Point dot on faceshape
                        for (Face r : faceActions) {
                            points = new float[106 * 2];
                            Rect rect = new Rect(CameraOverlap.PREVIEW_HEIGHT - r.left, r.top, CameraOverlap.PREVIEW_HEIGHT - r.right, r.bottom);
                            for (int i = 0; i < 106; i++) {
                                int x;
                                if (rotate270) {
                                    x = r.landmarks[i * 2];
                                } else {
                                    x = CameraOverlap.PREVIEW_HEIGHT - r.landmarks[i * 2];
                                }
                                int y = r.landmarks[i * 2 + 1];
                                points[i * 2] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                                points[i * 2 + 1] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                                if (i == 70) {
                                    p = new float[8];
                                    p[0] = view2openglX(x + 20, CameraOverlap.PREVIEW_HEIGHT);
                                    p[1] = view2openglY(y - 20, CameraOverlap.PREVIEW_WIDTH);
                                    p[2] = view2openglX(x - 20, CameraOverlap.PREVIEW_HEIGHT);
                                    p[3] = view2openglY(y - 20, CameraOverlap.PREVIEW_WIDTH);
                                    p[4] = view2openglX(x + 20, CameraOverlap.PREVIEW_HEIGHT);
                                    p[5] = view2openglY(y + 20, CameraOverlap.PREVIEW_WIDTH);
                                    p[6] = view2openglX(x - 20, CameraOverlap.PREVIEW_HEIGHT);
                                    p[7] = view2openglY(y + 20, CameraOverlap.PREVIEW_WIDTH);
                                }
                            }
                            if (p != null) {
                                break;
                            }
                        }
                        int tid = 0;
                        if (p != null) {
                            mBitmap.setPoints(p);
//                            tid = mBitmap.drawFrame();
                        }
                        mFrame.drawFrame(tid,mFramebuffer.drawFrameBuffer(),mFramebuffer.getMatrix());
                        if (points != null) {
                            mPoints.setPoints(points);
//                            mPoints.drawPoints();
                        }
                        mEglUtils.swap();
//                        if(points!= null) {
//                            for (int i = 0; i < 106 * 2; i++) {
//                                System.out.printf("point[%d] = %f\n", i, points[i]);
//                            }
//                        }
                    }
                });

                if(countdown){
                    countDownTimer.start();
                    mSocket.send("Ready");
                }

                if(touched){
                    getCount++;
                    String temp=null;
                    int format = camera.getParameters().getPreviewFormat();
//                    System.out.printf("format : %s",format);
                    int w = camera.getParameters().getPreviewSize().width;
                    int h = camera.getParameters().getPreviewSize().height;
                    byte[] yuv = rotateYUV420Degree270(data,w,h);

                    YuvImage yuvImage = new YuvImage(yuv, format, h, w, null);
                    Rect rect = new Rect(0, 0, h, w);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(rect, 100, outputStream);
                    byte[] arr = outputStream.toByteArray();
                    try{
                        System.gc();
                        temp= Base64.encodeToString(arr, Base64.DEFAULT);
                    }catch(Exception e){
                        e.printStackTrace();
                    }catch(OutOfMemoryError e){
                        temp=Base64.encodeToString(arr, Base64.DEFAULT);
                        Log.e("EWN", "Out of memory error catched");
                    }

                    if(mSocket.connected() && getCount % 2 == 0){
                        mSocket.send(temp);
                    }
                }
//                if(allowedCapture){
//                    int format = camera.getParameters().getPreviewFormat();
//                    System.out.printf("format : %s",format);
//                    int w = camera.getParameters().getPreviewSize().width;
//                    int h = camera.getParameters().getPreviewSize().height;
//
//                    YuvImage yuvImage = new YuvImage(data, format, w, h, null);
//                    Rect rect = new Rect(0, 0, w, h);
//                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                    yuvImage.compressToJpeg(rect, 100, outputStream);
//
//                    bitmap = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size());
//
//
//                    System.out.println("just test-1");
//                    if(bitmap != null){
//                        FileOutputStream outputStream1;
//                        System.out.println("just test0");
//                        try {
//                            String filePath = "/sdcard/DCIM/Camera/test.jpg";
//                            System.out.printf("filepath : %s",filePath);
//                            File imageFile = new File(filePath);
//                            outputStream1 = new FileOutputStream(imageFile);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream1);
//                            outputStream1.flush();
//                            outputStream1.close();
//                            System.out.println("just test");
//                        }
//                        catch (FileNotFoundException e) {
//                            System.out.println("just test2");
//                            Log.e("Log", e.toString());
//                        }
//                        catch (IOException e) {
//                            System.out.println("just test3");
//                            Log.e("Log", e.toString());
//                        }
//                    }
//                    else{
//                        System.out.println("bitmap is null");
//                    }
//                    allowedCapture = false;
//                }
            }
        });

        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(final SurfaceHolder holder, int format, final int width, final int height) {
                Log.d("=============","surfaceChanged");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mEglUtils != null){
                            mEglUtils.release();
                        }
                        mEglUtils = new EGLUtils();
                        mEglUtils.initEGL(holder.getSurface());
                        mFramebuffer.initFramebuffer();
                        mFrame.initFrame();
                        mFrame.setSize(width,height, CameraOverlap.PREVIEW_HEIGHT,CameraOverlap.PREVIEW_WIDTH );
                        mPoints.initPoints();
                        mBitmap.initFrame(CameraOverlap.PREVIEW_HEIGHT,CameraOverlap.PREVIEW_WIDTH);
                        cameraOverlap.openCamera(mFramebuffer.getSurfaceTexture());
                    }
                });

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mSocket.connected()) {
                            mSocket.disconnect();
                        }
                        cameraOverlap.release();
                        mFramebuffer.release();
                        mFrame.release();
                        mPoints.release();
                        mBitmap.release();
                        if(mEglUtils != null){
                            mEglUtils.release();
                            mEglUtils = null;
                        }
                    }
                });

            }
        });
        if(mSurfaceView.getHolder().getSurface()!= null &&mSurfaceView.getWidth() > 0){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mEglUtils != null){
                        mEglUtils.release();
                    }
                    mEglUtils = new EGLUtils();
                    mEglUtils.initEGL(mSurfaceView.getHolder().getSurface());
                    mFramebuffer.initFramebuffer();
                    mFrame.initFrame();
                    mFrame.setSize(mSurfaceView.getWidth(),mSurfaceView.getHeight(), CameraOverlap.PREVIEW_HEIGHT,CameraOverlap.PREVIEW_WIDTH );
                    mPoints.initPoints();
                    mBitmap.initFrame(CameraOverlap.PREVIEW_HEIGHT,CameraOverlap.PREVIEW_WIDTH);
                    cameraOverlap.openCamera(mFramebuffer.getSurfaceTexture());
                }
            });
        }
    }
    private float view2openglX(int x,int width){
        float centerX = width/2.0f;
        float t = x - centerX;
        return t/centerX;
    }
    private float view2openglY(int y,int height){
        float centerY = height/2.0f;
        float s = centerY - y;
        return s/centerY;
    }

    public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth)
                        + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    private static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        int count = 0;
        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
            yuv[count] = data[i];
            count++;
        }
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
                * imageHeight; i -= 2) {
            yuv[count++] = data[i - 1];
            yuv[count++] = data[i];
        }
        return yuv;
    }

    public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth,
                                               int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int nWidth = 0, nHeight = 0;
        int wh = 0;
        int uvHeight = 0;
        if (imageWidth != nWidth || imageHeight != nHeight) {
            nWidth = imageWidth;
            nHeight = imageHeight;
            wh = imageWidth * imageHeight;
            uvHeight = imageHeight >> 1;// uvHeight = height / 2
        }
        // ??Y
        int k = 0;
        for (int i = 0; i < imageWidth; i++) {
            int nPos = 0;
            for (int j = 0; j < imageHeight; j++) {
                yuv[k] = data[nPos + i];
                k++;
                nPos += imageWidth;
            }
        }
        for (int i = 0; i < imageWidth; i += 2) {
            int nPos = wh;
            for (int j = 0; j < uvHeight; j++) {
                yuv[k] = data[nPos + i];
                yuv[k + 1] = data[nPos + i + 1];
                k += 2;
                nPos += imageWidth;
            }
        }
        return rotateYUV420Degree180(yuv, imageWidth, imageHeight);
    }
}
