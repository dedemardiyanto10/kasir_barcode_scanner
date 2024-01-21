package com.kasir.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CameraSelectorDialogFragment extends DialogFragment {
    public interface CameraSelectorDialogListener {
        public void onCameraSelected(int cameraId);
    }

    private int mCameraId;
    private CameraSelectorDialogListener mListener;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setRetainInstance(true);
    }

    public static CameraSelectorDialogFragment newInstance(
            CameraSelectorDialogListener listener, int cameraId) {
        CameraSelectorDialogFragment fragment = new CameraSelectorDialogFragment();
        fragment.mCameraId = cameraId;
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mListener == null) {
            dismiss();
            return null;
        }

        int numberOfCameras = Camera.getNumberOfCameras();
        String[] cameraNames = new String[numberOfCameras];
        int checkedIndex = 0;

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraNames[i] = "Front Facing";
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraNames[i] = "Rear Facing";
            } else {
                cameraNames[i] = "Camera ID: " + i;
            }
            if (i == mCameraId) {
                checkedIndex = i;
            }
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle("Pilih Kamera")
                .setIcon(R.mipmap.ic_launcher)
                .setSingleChoiceItems(
                        cameraNames,
                        checkedIndex,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCameraId = which;
                            }
                        })
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (mListener != null) {
                                    mListener.onCameraSelected(mCameraId);
                                }
                            }
                        })
                .setNegativeButton(
                        "Batal",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {}
                        });

        return builder.create();
    }
}
