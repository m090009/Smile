package com.tohamy.smile.faceTrackerActivity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.vision.CameraSource;
import com.tohamy.smile.models.FileOperations;
import com.tohamy.smile.R;
import com.tohamy.smile.ui.SmileFaceDetector;
import com.tohamy.smile.models.ImageUtils;
import com.tohamy.smile.models.MTAnimations;

public class FaceTrackerActivity extends AppCompatActivity
        implements SmileFaceDetector.SmileTrackerCallback
        , CameraSource.PictureCallback{
    private static final int CAMERA_PERMISSION_REQUEST = 2;
    private static final String TAG = "FaceTrackerActivity";
    private SmileFaceDetector smileFaceDetector;
    private View backgroundView;
    private ImageView smileImage;
    private SimpleDraweeView imagePreview;
    private RelativeLayout imagePreviewCard;
    private ProgressBar smileMeter;
    private TextView funnyText;
    private final String IMAGE_PREVIEW_TAG = "ImagePreview";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_tracker);
        init();
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        this.checkForCameraPermission();
    }

    private void hideSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    protected void init(){
        this.initViews();
    }

    protected void initViews(){
        this.backgroundView = findViewById(R.id.viewBackground);
        this.smileImage = (ImageView)findViewById(R.id.smileImage);
        this.imagePreview = (SimpleDraweeView) findViewById(R.id.imagePreview);
        this.imagePreviewCard = (RelativeLayout) findViewById(R.id.imagePreviewCard);
        this.smileMeter = (ProgressBar) findViewById(R.id.smileMeter);
        this.smileMeter.getProgressDrawable().setColorFilter(
                Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
        this.smileMeter.getIndeterminateDrawable().setColorFilter(
                Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        this.smileMeter.setProgress(0);
        this.funnyText = (TextView) findViewById(R.id.funnyText);
        Typeface tf = Typeface.createFromAsset(getAssets()
                ,"fonts/KGBehindTheseHazelEyes.ttf");
        funnyText.setTypeface(tf);
    }

    protected void initFaceDetector(){
        this.smileFaceDetector = new SmileFaceDetector();
    }

    private void checkForCameraPermission(){
        int requestPermission = ActivityCompat.checkSelfPermission(this
                , Manifest.permission.CAMERA);
        if (requestPermission ==
                PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions
                    , this.CAMERA_PERMISSION_REQUEST);
            return;
        }

//        final Activity thisActivity = this;
//
//        View.OnClickListener listener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ActivityCompat.requestPermissions(thisActivity, permissions,
//                        RC_HANDLE_CAMERA_PERM);
//            }
//        };

//        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
//                Snackbar.LENGTH_INDEFINITE)
//                .setAction(R.string.ok, listener)
//                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {
        this.initFaceDetector();
        this.smileFaceDetector.init(this
                , findViewById(android.R.id.content)
                , CameraSource.CAMERA_FACING_FRONT
                , this);
    }

    @Override
    public void onPersonSmiling() {
        // Get a handler that can be used to post to the main thread
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                MTAnimations.captureAnimation(backgroundView
                        , smileImage);
            }
        };
        this.runOnUiThread(myRunnable);
        this.smileFaceDetector.takeAPicture(this);
//        this.smileFaceDetector.stopPreviewAndFreeCamera();
//        this.imagePreview.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPictureTaken(byte[] bytes) {
        boolean hasWritePermission = true;
        if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
            hasWritePermission = PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this
                    , Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(hasWritePermission) {
            this.smileFaceDetector.stopPreviewAndFreeCamera();
            FileOperations.saveImageToFileSystem(getApplicationContext()
                    , bytes, new FileOperations.AfterImageTaken() {
                        @Override
                        public void afterImageTaken(String imagePath) {
//                            imagePreview.setVisibility(View.VISIBLE);

                            imagePreviewCard.setVisibility(View.VISIBLE);
                            ImageUtils.requestImageResize(imagePreview.getWidth()
                                    , imagePreview.getHeight()
                                    , Uri.parse(ImageUtils.FRESCO_FILE
                                            + imagePath)
                                    , imagePreview);
                        }
                    });
        }
    }

    public void acceptPicture(View view){
        imagePreviewCard.setVisibility(View.INVISIBLE);
        this.smileFaceDetector.startCameraPreview();

    }

    @Override
    public void onUpdateFaces(final int numberOfFaces, final float happiness) {
        Runnable updateView = new Runnable() {
            @Override
            public void run() {
                int smilePercentage = (int)(happiness * 100);
                smileMeter.setProgress(smilePercentage);
                funnyText.setText(getTextForSmilePercentage(smilePercentage));
            }
        };
        this.runOnUiThread(updateView);

    }

    public String getTextForSmilePercentage(float smilePercentage){
        String funnyText = "";
        if (smilePercentage <= 30){
            funnyText = this.getString(R.string.encourage_comment_1);
        } else if (30 < smilePercentage && smilePercentage <= 60){
            funnyText = this.getString(R.string.tease_comment_1);
        } else if (60 < smilePercentage) {
            funnyText = this.getString(R.string.praise_comment_1);
        }
        return funnyText;
    }

    public void switchCamera(View view){
        this.smileFaceDetector.switchCameras();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
        if(this.smileFaceDetector != null)
            if (!this.smileFaceDetector.isOperational(this)) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Smile App")
                        .setMessage(R.string.isOperational_snackbar_title)
                        .setPositiveButton(R.string.isOperational_snackbar_action, listener)
                        .show();
            } else {
                this.smileFaceDetector.startCameraPreview();
            }
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(this.smileFaceDetector != null)
            this.smileFaceDetector.stopPreviewAndFreeCamera();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != CAMERA_PERMISSION_REQUEST) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Smile App")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.dialog_ok, listener)
                .show();
    }
}
