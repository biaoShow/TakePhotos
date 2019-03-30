package com.benxiang.takephotos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SurfaceView sv_preview;
    private Button btn_takephoto;
    private SurfaceHolder surfaceholder;
    private Camera camera = null;
    private CountDownTime countDownTime;
    private String path;
    private String logPath;
    private LogCatHelper logCatHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //申请相机权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        path = Environment.getExternalStorageDirectory() + "/DCIM/";
//        path = "mnt/external_sd/DCIM/";
        Log.i("MainActivity", path);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        sv_preview = findViewById(R.id.sv_preview);
        btn_takephoto = findViewById(R.id.btn_takephoto);
        surfaceholder = sv_preview.getHolder();
        countDownTime = new CountDownTime(10000, 1000);
        logPath = Environment.getExternalStorageDirectory() + "/Log2";
        logCatHelper = LogCatHelper.getInstance(this, logPath);

        logCatHelper.start();//日志保存

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPreview(); //开始预览
                    }
                });
            }
        }).start();

        btn_takephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTime.start();
                btn_takephoto.setClickable(false);
                Toast.makeText(MainActivity.this, "开始自动拍照", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置预览
     */
    private void setPreview() {
        openCamera();//打开预览摄像头
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceholder);
//                camera.setDisplayOrientation(90);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 前摄像头设置预览
     */
    private void setPreview2() {
        openCamera2();//打开预览摄像头
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceholder);
//                camera.setDisplayOrientation(90);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开后置摄像头
     */
    int j = 0;//标记打开摄像头2的失败次数
    private void openCamera() {
        int cameraCount = Camera.getNumberOfCameras();
        Log.i("摄像头个数",cameraCount+"");
        if (cameraCount >= 1 && camera == null) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (camera != null) {
                try {
                    j=0;
                    camera.setErrorCallback(callback);
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setJpegQuality(80);  //设置图片质量
                    parameters.setPictureSize(1920, 1080);
                    parameters.setPreviewSize(1920, 1080);
                    camera.setParameters(parameters);
                }catch(Exception e){
                    if(camera != null){
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                    openCamera();
                }

            }else{
                Log.i("MainActivity","打开相机1失败");
                j++;
                if(j<5){
                    openCamera();
                }else{
                    Log.i("MainActivity","打开相机1失败，打开相机2");
                    openCamera2();
                }
            }
            //判断是否支持输出YUV格式
//            List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
//            if(supportedPreviewFormats.size()>0){
//                Toast.makeText(MainActivity.this,"Camera1:"+supportedPreviewFormats.size(),Toast.LENGTH_SHORT).show();
//            }
//            摄像头支持的像素
//            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
//            for (Camera.Size a:supportedPictureSizes) {
//                Log.i("MainActivity1Picture","宽："+a.width+"高："+ a.height);
//            }
//            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
//            for (Camera.Size a:supportedPreviewSizes) {
//                Log.i("MainActivity1Preview","宽："+a.width+"高："+ a.height);
//            }

        }else{
            Log.i("MainActivity","没有检测到摄像头");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,"没有检测到摄像头,请检查连接",Toast.LENGTH_SHORT).show();
                }
            });
            try {
                Thread.sleep(5000);
                int RECameraCount = Camera.getNumberOfCameras();
                Log.i("MainActivity","从新检测摄像头个数="+RECameraCount);
                if(RECameraCount>=1){
                    openCamera();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开前置摄像头
     */
    int i = 0;//标记打开摄像头2的失败次数
    private void openCamera2() {
        int cameraCount = Camera.getNumberOfCameras();
        Log.i("摄像头个数",cameraCount+"");
        if (cameraCount >= 2 && camera == null) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            if (camera != null) {
                try{
                    i=0;
                    camera.setErrorCallback(callback);
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setJpegQuality(80);  //设置图片质量
                    parameters.setPictureSize(1920, 1080);
                    parameters.setPreviewSize(1920, 1080);
                    camera.setParameters(parameters);
                }catch (Exception e){
                    if(camera != null){
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                    openCamera2();
                }
            }else{
                Log.i("MainActivity","打开相机2失败");
                i++;
                if(i<5){
                    openCamera2();
                }else{
                    Log.i("MainActivity","打开相机2失败，打开相机1");
                    openCamera();
                }
            }
            //判断是否支持输出YUV格式
//            final List<Integer> supportedPreviewFormats = parameters.getSupportedPreviewFormats();
//            if(supportedPreviewFormats.size()>0){
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this,"Camera2:"+supportedPreviewFormats.size(),Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//            }
//            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
//            for (Camera.Size a:supportedPictureSizes) {
//                Log.i("MainActivity2Picture","宽："+a.width+"高："+ a.height);
//            }
//            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
//            for (Camera.Size a:supportedPreviewSizes) {
//                Log.i("MainActivity2Preview","宽："+a.width+"高："+ a.height);
//            }

        } else {
            openCamera();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        if (countDownTime != null) {
            countDownTime.cancel();
        }
    }

    /**
     * 倒计时类
     */
    class CountDownTime extends CountDownTimer {
        int i = 0;

        public CountDownTime(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (camera != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        camera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(final byte[] data, final Camera camera) {
                                Log.i("MainActivity","onPictureTaken保存");
                                FileOutputStream fileOutputStream = null;
                                try {
                                    fileOutputStream = new FileOutputStream(path + System.currentTimeMillis() + ".jpg");
                                    fileOutputStream.write(data);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        fileOutputStream.close();
                                        if (MainActivity.this.camera != null) {
                                            MainActivity.this.camera.stopPreview();
                                            MainActivity.this.camera.release();
                                            MainActivity.this.camera = null;
                                        }
                                        Thread.sleep(1000);
                                        if (i % 2 == 0) {
                                            setPreview2();
                                        } else {
                                            setPreview();
                                        }
                                        i++;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                btn_takephoto.setText("成功保存:" + i);
                                            }
                                        });
                                        countDownTime.start();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }).start();
            }
        }
    }

    //监听相机服务是否终止或者出现其他异常
    Camera.ErrorCallback callback = new Camera.ErrorCallback() {
        @Override
        public void onError(int error, Camera camera) {
            Log.d("onError", "to do something");
            if (MainActivity.this.camera != null) {
                MainActivity.this.camera.stopPreview();
                MainActivity.this.camera.release();
                MainActivity.this.camera = null;
            }
            if (countDownTime != null) {
                countDownTime.cancel();
            }
            setPreview();
            countDownTime.start();
        }
    };
}
