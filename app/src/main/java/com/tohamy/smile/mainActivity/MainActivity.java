package com.tohamy.smile.mainActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.FaceDetector;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tohamy.smile.faceTrackerActivity.FaceTrackerActivity;
import com.tohamy.smile.mainActivity.Interfaces.FetchAlbumAndImagesInterface;
import com.tohamy.smile.R;
import com.tohamy.smile.models.FileOperations;
import com.tohamy.smile.models.Item;
import com.tohamy.smile.models.MediaStoreOperations;

import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener
        , FetchAlbumAndImagesInterface{
//    private SimpleDraweeView smileImage;
    private FaceDetector faceDetector;
    private Canvas tempCanvas;
    private final int CAMERA_PERMISSION_REQUEST = 1;
    private final int STORAGE_READ_PERMISSION_REQUEST = 2;
    private final int STORAGE_WRITE_PERMISSION_REQUEST = 3;
    private final int STORAGE_REQUESTS = 2;
    private View backgroundView;
    private ImageView smileImage;
    private SmilesRecyclerView smilesRecyclerView;
    private TwoWayView mosaicView;
    private MosaicLayoutAdapter mosaicLayoutAdapter;
    private LinearLayout emptyMosaicView;
    private TextView emptyMosaicText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M)
            askForStoragePermissions();
        else
            getSmileAlbumImages();
    }

    private void init(){
        initViews();
    }

    private void initViews(){
        this.smileImage = (ImageView)findViewById(R.id.smileImage);
        this.backgroundView = findViewById(R.id.viewBackground);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
//        this.smilesRecyclerView = new SmilesRecyclerView();
//        this.smilesRecyclerView.setUpRecyclerView(
//                (RecyclerView) findViewById(R.id.recyclerView));
        this.mosaicView = (TwoWayView) findViewById(R.id.mosaicView);
        this.mosaicLayoutAdapter = new MosaicLayoutAdapter(this
                , this.mosaicView
                , new ArrayList<Item>()
                , getBlockSize());
        this.mosaicView.setAdapter(this.mosaicLayoutAdapter);
        this.emptyMosaicView = (LinearLayout) findViewById(R.id.emptyMosaicView);
        this.emptyMosaicText = (TextView) findViewById(R.id.emptyMosaicText);
        setupTwoWayView();
    }

    public int getBlockSize(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int block = width/3;
        return block;
    }

    public void setupTwoWayView(){
        //INIT Initializing the viewRecycler
        mosaicView.setHasFixedSize(true);
        mosaicView.setHapticFeedbackEnabled(true);
        mosaicView.setLongClickable(true);
        mosaicView.setItemAnimator(new DefaultItemAnimator());
    }

    public void askForStoragePermissions(){
        if ( PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this
                , Manifest.permission.READ_EXTERNAL_STORAGE) &&
                PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this
                , Manifest.permission.READ_EXTERNAL_STORAGE)){
            final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions
                        , this.STORAGE_REQUESTS);
                return;
        }else{
            getSmileAlbumImages();
        }
    }

    @Override
    public void onClick(View view) {
        launchFaceTracker();
    }

    public void launchFaceTracker(){
        Intent intent = new Intent(this
                , FaceTrackerActivity.class);
        startActivity(intent);
    }

    @Override
    public void getSmileAlbumImages() {
        FileOperations.getOrCreateFolder(this);
        Cursor cur = MediaStoreOperations
                    .getGalleryAlbumsCursor(this);
        ArrayList<Item> smileImages = MediaStoreOperations
                .getSmileAlbumFromCursor(cur);
        this.mosaicLayoutAdapter.repopulate(smileImages);
        if(smileImages.size() == 0){
            onEmptyMosaicView();
        }else{
            this.emptyMosaicView.setVisibility(View.INVISIBLE);
        }
//        this.smilesRecyclerView.populate(galleryItems);
    }

    public void onEmptyMosaicView(){
        Typeface tf = Typeface.createFromAsset(getAssets()
                ,"fonts/KGBehindTheseHazelEyes.ttf");
        this.emptyMosaicText.setTypeface(tf);
        this.emptyMosaicView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUESTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSmileAlbumImages();
                }else{
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Smile App")
                            .setMessage(R.string.no_storage_permission)
                            .setPositiveButton(R.string.dialog_ok, listener)
                            .show();
                }
                return ;
            }
        }
    }
}
