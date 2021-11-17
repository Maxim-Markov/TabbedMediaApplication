package com.highresults.tabbedmediaapplication.ui.main;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.highresults.tabbedmediaapplication.MainActivity;
import com.highresults.tabbedmediaapplication.MediaPlayerFragment;

public class DialogFrag extends DialogFragment {
    public DialogFrag() {
        // Required empty public constructor
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Важное сообщение!")
                .setMessage("Приложению необходим доступ к мультмиедиа, чтобы проиграть композицию. Активируйте разрешение для хранилища вручную")
                .setIcon(android.R.drawable.stat_sys_warning)
                .setPositiveButton("Перейти к настройкам разрешений", (dialog, id) -> {

                    MediaPlayerFragment f = (MediaPlayerFragment) (requireActivity().getSupportFragmentManager().findFragmentByTag("f" + MainActivity.viewPager.getCurrentItem()));
                    if(f != null) {
                        f.launch();
                    }
                })
                .setNegativeButton("выход",
                        (dialog, id) -> dialog.cancel());
        builder.setCancelable(true);
        return builder.create();
    }

    public static DialogFrag newInstance() {
        return new DialogFrag();
    }


}