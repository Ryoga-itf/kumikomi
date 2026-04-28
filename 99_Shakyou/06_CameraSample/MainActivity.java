import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.camerasample.R;

import java.util.Arrays;

CameraDevice mCameraDevice = null;
TextureView cameraPreview;
Button cameraButton;
CameraCaptureSession mCaptureSession = null;
CaptureRequest mPreviewRequest = null;
CaptureRequest.Builder mPreviewRequestBuilder = null;
String FrontCameraId;
String BackCameraId;
String currCameraId;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CameraManager mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        FrontCameraId = null;
        BackCameraId = null;
        try {
            String[] cameraIdList = mCameraManager.getCameraIdList();
            for (String cameraId: cameraIdList) {
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                switch (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    case CameraCharacteristics.LENS_FACING_FRONT:
                        FrontCameraId = cameraId;
                        Log.d("CameraSample", "Front camera found.");
                        break;
                    case CameraCharacteristics.LENS_FACING_BACK:
                        BackCameraId = cameraId;
                        Log.d("CameraSample", "Back camera found.");
                        break;
                    default:
                }
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

        if (FrontCameraId != null) currCameraId = FrontCameraId;
        else currCameraId = BackCameraId;

        cameraButton = (Button) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currCameraId == FrontCameraId && BackCameraId != null) {
                    currCameraId = BackCameraId;
                    cameraButton.setText("Change to Front Camera");
                }
                else if (currCameraId == BackCameraId && FrontCameraId != null) {
                    currCameraId = FrontCameraId;
                    cameraButton.setText("Change to Back Camera");
                }
                else return;

                mCaptureSession.close();
                mCameraDevice.close();
                try {
                    mCameraManager.openCamera(currCameraId, mStateCallback, null);
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        cameraPreview = (TextureView) findViewById(R.id.cameraPreview);
        cameraPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                try {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        Log.d("Camera Sample", "Camera is not permitted.");
                        return;
                    }
                    mCameraManager.openCamera(currCameraId, mStateCallback, null);
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                Log.d("Camera Sample", "onSurfaceTextureSizeChanged");
            }
            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                Log.d("Camera Sample", "onSurfaceTextureDestroyed");
                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            }
        });
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            try {
                SurfaceTexture texture = cameraPreview.getSurfaceTexture();
                texture.setDefaultBufferSize(cameraPreview.getWidth(), cameraPreview.getHeight());
                Surface surface = new Surface(texture);
                mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(surface);
                mCameraDevice.createCaptureSession(Arrays.asList(surface),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                mCaptureSession = cameraCaptureSession;
                                try {
                                    mPreviewRequest = mPreviewRequestBuilder.build();
                                    mCaptureSession.setRepeatingRequest(mPreviewRequest, null, null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                Log.e("CameraSample", "error onConfigureFailed");
                            }
                        }, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }
        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };
}