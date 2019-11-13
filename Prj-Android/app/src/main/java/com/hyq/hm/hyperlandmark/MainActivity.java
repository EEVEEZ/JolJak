package com.hyq.hm.hyperlandmark;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.samples.hellosceneform.R;
import com.google.ar.sceneform.ux.AugmentedFaceNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private FaceArFragment arFragment;
    private ModelRenderable faceRegionsRenderable;
    private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();
    private AugmentedFaceNode faceNode = new AugmentedFaceNode();

    private ArSceneView sceneView;

    private Button button;
    private boolean touched;
    private boolean ready;
    private int count;
    private int result;
    private int PICK_IMAGE_REQUEST = 1;
    private int typeReady = 0;
    private int typeFace = 1;
    private int typeCap = 2;

    private ImageButton imageButton0;
    private ImageButton imageButton1;
    private ImageButton imageButton2;
    private ImageButton imageButton3;

    private ViewPager viewPager;
    private ViewpagerAdapter viewpagerAdapter;

    private int Selectraw[][] = {
            {R.raw.nocap},
            {R.raw.blackcap, R.raw.whitecap, R.raw.bluecap, R.raw.pinkcap},
            {R.raw.jungblue, R.raw.jungbrown, R.raw.jungco, R.raw.jungpurple},
            {R.raw.beregray, R.raw.beremopi, R.raw.beremowh},
    };

    private String[] denied;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET};

    public jjWebsocket mWebsocket;

    View.OnClickListener Capturelistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(viewpagerAdapter.getIndex() != 0){
                Toast toast = Toast.makeText(getApplicationContext(), "Please take off the hat", Toast.LENGTH_LONG);
                toast.show();
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), "Caputured", Toast.LENGTH_LONG);
                toast.show();
                touched = true;
                ready = true;
                count = 0;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (PermissionChecker.checkSelfPermission(this, permissions[i]) == PermissionChecker.PERMISSION_DENIED) {
                    list.add(permissions[i]);
                }
            }
            if (list.size() != 0) {
                denied = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    denied[i] = list.get(i);
                }
                ActivityCompat.requestPermissions(this, denied, 5);
            }
        }
        setContentView(R.layout.activity_main);


        imageButton0 = (ImageButton) findViewById(R.id.ImageButton0);
        imageButton1 = (ImageButton) findViewById(R.id.ImageButton1);
        imageButton2 = (ImageButton) findViewById(R.id.ImageButton2);
        imageButton3 = (ImageButton) findViewById(R.id.ImageButton3);

        imageButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager = (ViewPager) findViewById(R.id.pager);
                viewpagerAdapter = new ViewpagerAdapter(MainActivity.this, 0);
                viewPager.setAdapter(viewpagerAdapter);
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
                tabLayout.setupWithViewPager(viewPager, true);
            }
        });

        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager = (ViewPager) findViewById(R.id.pager);
                viewpagerAdapter = new ViewpagerAdapter(MainActivity.this, 1);
                viewPager.setAdapter(viewpagerAdapter);
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
                tabLayout.setupWithViewPager(viewPager, true);
            }
        });

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager = (ViewPager) findViewById(R.id.pager);
                viewpagerAdapter = new ViewpagerAdapter(MainActivity.this, 2);
                viewPager.setAdapter(viewpagerAdapter);
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
                tabLayout.setupWithViewPager(viewPager, true);
            }
        });

        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager = (ViewPager) findViewById(R.id.pager);
                viewpagerAdapter = new ViewpagerAdapter(MainActivity.this, 3);
                viewPager.setAdapter(viewpagerAdapter);
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
                tabLayout.setupWithViewPager(viewPager, true);
            }
        });

        arFragment = (FaceArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        button = findViewById(R.id.Capture);
        button.setOnClickListener(Capturelistener);

        Button albumButton = findViewById(R.id.Album_button);
        albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewpagerAdapter = new ViewpagerAdapter(this, 0);
        viewPager.setAdapter(viewpagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);

        mWebsocket = new jjWebsocket();

        mWebsocket.init();

        sceneView = arFragment.getArSceneView();
        sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);

        Scene scene = sceneView.getScene();

        scene.addOnUpdateListener(
                (FrameTime frameTime) -> {
                    System.out.println(viewpagerAdapter.getIndex());
                    ModelRenderable.builder()
                            .setSource(this, Selectraw[viewpagerAdapter.getIndex()][viewPager.getCurrentItem()])
                            .build()
                            .thenAccept(modelRenderable -> {
                                faceRegionsRenderable = modelRenderable;
                                modelRenderable.setShadowCaster(false);
                                modelRenderable.setShadowReceiver(false);
                            });

//                    System.out.println(viewPager.getCurrentItem());
                    if (mWebsocket.Detected) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Wait For Result...", Toast.LENGTH_LONG);
                        toast.show();
                        mWebsocket.Detected = false;
                    } else if (mWebsocket.Face_Predict) {
//                        System.out.println("mWebsocket.result = ");
//                        System.out.println(mWebsocket.result);
                        mWebsocket.Face_Predict = false;
                        if (mWebsocket.result.equals("long")) {
                            result = 1;
                        } else if (mWebsocket.result.equals("rectangle")) {
                            result = 2;
                        } else if (mWebsocket.result.equals("round")) {
                            result = 3;
                        }
                        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                        intent.putExtra("result", result);
                        startActivity(intent);
                    } else if (mWebsocket.Cap_Predict) {
                        mWebsocket.Cap_Predict = false;
                        if (mWebsocket.result.equals("beret")) {
                            Toast toast = Toast.makeText(getApplicationContext(), "That is Beret", Toast.LENGTH_LONG);
                            toast.show();
                        } else if (mWebsocket.result.equals("softhat")) {
                            Toast toast = Toast.makeText(getApplicationContext(), "That is Softhat", Toast.LENGTH_LONG);
                            toast.show();
                        } else if (mWebsocket.result.equals("ballcap")) {
                            Toast toast = Toast.makeText(getApplicationContext(), "That is BallCap", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    } else if (mWebsocket.Fail) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Please Recapture", Toast.LENGTH_LONG);
                        toast.show();
                        mWebsocket.Fail = false;
                    }
                    if (ready) {
                        mWebsocket.send("Ready", typeReady);
                        ready = false;
                    }
                    if (touched && count < 1) {
                        count++;
                        Bitmap bitmap = Bitmap.createBitmap(sceneView.getWidth(), sceneView.getHeight(), Bitmap.Config.ARGB_8888);
                        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
                        handlerThread.start();
                        PixelCopy.request(sceneView, bitmap, (copyResult) -> {
                            if (copyResult == PixelCopy.SUCCESS) {
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                byte[] compressedData = outputStream.toByteArray();
                                String sendString = null;
                                try {
                                    System.gc();
                                    sendString = Base64.encodeToString(compressedData, Base64.DEFAULT);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } catch (OutOfMemoryError e) {
                                    sendString = Base64.encodeToString(compressedData, Base64.DEFAULT);
                                    Log.e("EWN", "Out of memory error catched");
                                }
                                if (mWebsocket.connected()) {
                                    mWebsocket.send(sendString, typeFace);
                                }
                                touched = false;
                            }
                        }, new Handler(handlerThread.getLooper()));
                    }

                    if (faceRegionsRenderable == null) {
                        return;
                    }

                    Collection<AugmentedFace> faceList =
                            sceneView.getSession().getAllTrackables(AugmentedFace.class);


                    // Make new AugmentedFaceNodes for any new faces.
                    for (AugmentedFace face : faceList) {
                        Pose nosePose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP);
                        Pose foreheadLeft = face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_LEFT);
                        Pose ForeheadRight = face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_RIGHT);
                        float[] noseTranslation = nosePose.getTranslation();
                        float[] ForeheadLeftTranslation = foreheadLeft.getTranslation();
                        float[] ForeheadRightTranslation = ForeheadRight.getTranslation();
                        float leftx = Math.abs(Math.abs(noseTranslation[0]) - Math.abs(ForeheadLeftTranslation[0]));
                        float rightx = Math.abs(Math.abs(ForeheadRightTranslation[0]) - Math.abs(noseTranslation[0]));
                        float ave = 55f + ((leftx + rightx) / 2);
                        float caph = (Math.abs(ForeheadLeftTranslation[1]) + Math.abs(ForeheadRightTranslation[1])) / 2 + (Math.abs(noseTranslation[1]) / 2);
                        Vector3 localPosition = new Vector3();
                        localPosition.set(0.0f, caph, -0.015f);
                        faceNode.setParent(null);
                        faceNode.setAugmentedFace(face);
                        faceNode.setLocalScale(new Vector3(ave - 12.3f, ave, ave));
                        faceNode.setFaceRegionsRenderable(faceRegionsRenderable);
                        faceNode.setParent(scene);
                        faceNodeMap.put(face, faceNode);
                    }

                    // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
                    Iterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> iter =
                            faceNodeMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<AugmentedFace, AugmentedFaceNode> entry = iter.next();
                        AugmentedFace face = entry.getKey();
                        if (face.getTrackingState() == TrackingState.STOPPED) {
                            AugmentedFaceNode faceNode = entry.getValue();
                            faceNode.setParent(null);
                            iter.remove();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                if (mWebsocket.connected()) {
                    mWebsocket.send("Ready", typeReady);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    byte[] compressedData = outputStream.toByteArray();
                    String sendString = null;
                    try {
                        System.gc();
                        sendString = Base64.encodeToString(compressedData, Base64.DEFAULT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError e) {
                        sendString = Base64.encodeToString(compressedData, Base64.DEFAULT);
                        Log.e("EWN", "Out of memory error catched");
                    }
                    mWebsocket.send(sendString, typeCap);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
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
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//    private Image imageFromFrame(Frame frame) throws NotYetAvailableException {
//        if (frame == null) {
//            Log.e("error", "Frame Is Null");
//            return null;
//        }
//        Image image = frame.acquireCameraImage();
//        return image;
//    }
//
//    private static byte[] YUV420toNV21(Image image) {
//        byte[] nv21;
//        // Get the three planes.
//        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
//        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
//        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
//
//        int ySize = yBuffer.remaining();
//        int uSize = uBuffer.remaining();
//        int vSize = vBuffer.remaining();
//
//        nv21 = new byte[ySize + uSize + vSize];
//
//        //U and V are swapped
//        yBuffer.get(nv21, 0, ySize);
//        vBuffer.get(nv21, ySize, uSize);
//        uBuffer.get(nv21, ySize + uSize, vSize);
//
//        return nv21;
//    }
//
//    public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
//        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
//        // Rotate the Y luma
//        int i = 0;
//        for (int x = 0; x < imageWidth; x++) {
//            for (int y = imageHeight - 1; y >= 0; y--) {
//                yuv[i] = data[y * imageWidth + x];
//                i++;
//            }
//        }
//        // Rotate the U and V color components
//        i = imageWidth * imageHeight * 3 / 2 - 1;
//        for (int x = imageWidth - 1; x > 0; x = x - 2) {
//            for (int y = 0; y < imageHeight / 2; y++) {
//                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
//                i--;
//                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth)
//                        + (x - 1)];
//                i--;
//            }
//        }
//        return yuv;
//    }
//
//    private static byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
//        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
//        int i = 0;
//        int count = 0;
//        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
//            yuv[count] = data[i];
//            count++;
//        }
//        i = imageWidth * imageHeight * 3 / 2 - 1;
//        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
//                * imageHeight; i -= 2) {
//            yuv[count++] = data[i - 1];
//            yuv[count++] = data[i];
//        }
//        return yuv;
//    }
//
//    public static byte[] rotateYUV420Degree270(byte[] data, int imageWidth,
//                                               int imageHeight) {
//        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
//        int nWidth = 0, nHeight = 0;
//        int wh = 0;
//        int uvHeight = 0;
//        if (imageWidth != nWidth || imageHeight != nHeight) {
//            nWidth = imageWidth;
//            nHeight = imageHeight;
//            wh = imageWidth * imageHeight;
//            uvHeight = imageHeight >> 1;// uvHeight = height / 2
//        }
//        // ??Y
//        int k = 0;
//        for (int i = 0; i < imageWidth; i++) {
//            int nPos = 0;
//            for (int j = 0; j < imageHeight; j++) {
//                yuv[k] = data[nPos + i];
//                k++;
//                nPos += imageWidth;
//            }
//        }
//        for (int i = 0; i < imageWidth; i += 2) {
//            int nPos = wh;
//            for (int j = 0; j < uvHeight; j++) {
//                yuv[k] = data[nPos + i];
//                yuv[k + 1] = data[nPos + i + 1];
//                k += 2;
//                nPos += imageWidth;
//            }
//        }
//        return rotateYUV420Degree180(yuv, imageWidth, imageHeight);
//    }


    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}