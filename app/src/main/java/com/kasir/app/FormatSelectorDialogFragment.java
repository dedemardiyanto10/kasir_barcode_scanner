package com.kasir.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class FormatSelectorDialogFragment extends DialogFragment {
    public interface FormatSelectorDialogListener {
        public void onFormatsSaved(ArrayList<Integer> selectedIndices);
    }

    private ArrayList<Integer> mSelectedIndices;
    private FormatSelectorDialogListener mListener;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setRetainInstance(true);
    }

    public static FormatSelectorDialogFragment newInstance(
            FormatSelectorDialogListener listener, ArrayList<Integer> selectedIndices) {
        FormatSelectorDialogFragment fragment = new FormatSelectorDialogFragment();
        if (selectedIndices == null) {
            selectedIndices = new ArrayList<Integer>();
        }
        fragment.mSelectedIndices = new ArrayList<Integer>(selectedIndices);
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mSelectedIndices == null || mListener == null) {
            dismiss();
            return null;
        }

        String[] formats = new String[ZXingScannerView.ALL_FORMATS.size()];
        boolean[] checkedIndices = new boolean[ZXingScannerView.ALL_FORMATS.size()];
        int i = 0;
        for (BarcodeFormat format : ZXingScannerView.ALL_FORMATS) {
            formats[i] = format.toString();
            if (mSelectedIndices.contains(i)) {
                checkedIndices[i] = true;
            } else {
                checkedIndices[i] = false;
            }
            i++;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle("Choose Format")
                .setIcon(R.mipmap.ic_launcher)
                .setMultiChoiceItems(
                        formats,
                        checkedIndices,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    mSelectedIndices.add(which);
                                } else if (mSelectedIndices.contains(which)) {
                                    mSelectedIndices.remove(mSelectedIndices.indexOf(which));
                                }
                            }
                        })
                .setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (mListener != null) {
                                    mListener.onFormatsSaved(mSelectedIndices);
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
