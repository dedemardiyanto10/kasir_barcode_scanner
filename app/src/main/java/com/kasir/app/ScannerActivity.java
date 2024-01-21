package com.kasir.app;

import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.util.ArrayList;
import java.util.List;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity
        implements ZXingScannerView.ResultHandler,
                MessageDialogFragment.MessageDialogListener,
                FormatSelectorDialogFragment.FormatSelectorDialogListener,
                CameraSelectorDialogFragment.CameraSelectorDialogListener {
    private ZXingScannerView mScannerView;

    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String SELECTED_FORMATS = "SELECTED_FORMATS";
    private static final String CAMERA_ID = "CAMERA_ID";

    private boolean mFlash;
    private boolean mAutoFocus;
    private ArrayList<Integer> mSelectedIndices;
    private int mCameraId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mFlash = savedInstanceState.getBoolean(FLASH_STATE, false);
            mAutoFocus = savedInstanceState.getBoolean(AUTO_FOCUS_STATE, true);
            mSelectedIndices = savedInstanceState.getIntegerArrayList(SELECTED_FORMATS);
            mCameraId = savedInstanceState.getInt(CAMERA_ID, -1);
        } else {
            mFlash = false;
            mAutoFocus = true;
            mSelectedIndices = null;
            mCameraId = -1;
        }

        setContentView(R.layout.activity_scanner);
        setupToolbar();

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        mScannerView.setAspectTolerance(0.5f);
        setupFormats();
        contentFrame.addView(mScannerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus);
        outState.putIntegerArrayList(SELECTED_FORMATS, mSelectedIndices);
        outState.putInt(CAMERA_ID, mCameraId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_scanner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_flash) {
            mFlash = !mFlash;
            item.setIcon(mFlash ? R.drawable.flash_on : R.drawable.flash_off);
            mScannerView.setFlash(mFlash);
        }
        if (itemId == R.id.action_focus) {
            mAutoFocus = !mAutoFocus;
            item.setIcon(mAutoFocus ? R.drawable.focus_on : R.drawable.focus_off);
            mScannerView.setAutoFocus(mAutoFocus);
            return true;
        }
        if (itemId == R.id.action_format) {
            DialogFragment fragment =
                    FormatSelectorDialogFragment.newInstance(this, mSelectedIndices);
            fragment.show(getSupportFragmentManager(), "format_selector");
        }
        if (itemId == R.id.action_camera) {
            mScannerView.stopCamera();
            DialogFragment cFragment = CameraSelectorDialogFragment.newInstance(this, mCameraId);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            cFragment.show(ft, "camera_selector");
        }

        return true;
    }

    public void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void handleResult(Result rawResult) {
        int mp3ResourceId = R.raw.scan;
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), mp3ResourceId);
        mediaPlayer.start();
        Intent intent = new Intent();
        intent.putExtra("result", rawResult.getText());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void showMessageDialog(String message) {
        DialogFragment fragment = MessageDialogFragment.newInstance("Scan Results", message, this);
        fragment.show(getSupportFragmentManager(), "scan_results");
    }

    public void closeMessageDialog() {
        closeDialog("scan_results");
    }

    public void closeFormatsDialog() {
        closeDialog("format_selector");
    }

    public void closeDialog(String dialogName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(dialogName);
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    public void onDialogPositiveClick(DialogFragment dialog) {
        mScannerView.resumeCameraPreview(this);
    }

    public void onFormatsSaved(ArrayList<Integer> selectedIndices) {
        mSelectedIndices = selectedIndices;
        setupFormats();
    }

    public void onCameraSelected(int cameraId) {
        mCameraId = cameraId;
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    public void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        if (mSelectedIndices == null || mSelectedIndices.isEmpty()) {
            mSelectedIndices = new ArrayList<Integer>();
            for (int i = 0; i < ZXingScannerView.ALL_FORMATS.size(); i++) {
                mSelectedIndices.add(i);
            }
        }

        for (int index : mSelectedIndices) {
            formats.add(ZXingScannerView.ALL_FORMATS.get(index));
        }
        if (mScannerView != null) {
            mScannerView.setFormats(formats);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        closeMessageDialog();
        closeFormatsDialog();
    }
}
