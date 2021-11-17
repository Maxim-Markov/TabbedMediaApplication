package com.highresults.tabbedmediaapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.highresults.tabbedmediaapplication.ui.main.AFListener;
import com.highresults.tabbedmediaapplication.ui.main.DialogFrag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.AUDIO_SERVICE;

public class MediaPlayerFragment extends Fragment implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private boolean isshowdialog = false;
    final String LOG_TAG = "myLogs";
    final String DATA_HTTP = "https://zvukogram.com/index.php?r=site/download&id=72807";
    private Uri DATA_URI = null;
    private final int music_num = 0;
    AFListener afListenerMusic;
    AudioFocusRequest focusRequest;
    MediaPlayer mediaPlayer;
    AudioManager am;
    CheckBox chbLoop;
    Button button_http;
    Button button_storage;
    Button button_pause;
    Button button_resume;
    Button button_stop;
    Button button_back;
    Button button_forw;
    Button button_raw;
    Button button_info;


    public static MediaPlayerFragment newInstance() {
        return new MediaPlayerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        am = (AudioManager) requireContext().getSystemService(AUDIO_SERVICE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.media_player_fragment, container, false);
        chbLoop = view.findViewById(R.id.chbLoop);
        button_http = view.findViewById(R.id.btnStartHttp);
        button_raw = view.findViewById(R.id.btnStartRaw);
        button_storage = view.findViewById(R.id.btnStartUri);
        button_forw = view.findViewById(R.id.btnForward);
        button_back = view.findViewById(R.id.btnBackward);
        button_pause = view.findViewById(R.id.btnPause);
        button_stop = view.findViewById(R.id.btnStop);
        button_resume = view.findViewById(R.id.btnResume);
        button_info = view.findViewById(R.id.btnInfo);
        button_http.setOnClickListener(this::onClickStart);
        button_raw.setOnClickListener(this::onClickStart);
        button_storage.setOnClickListener(this::onClickStart);
        button_forw.setOnClickListener(this::onClick);
        button_back.setOnClickListener(this::onClick);
        button_pause.setOnClickListener(this::onClick);
        button_resume.setOnClickListener(this::onClick);
        button_stop.setOnClickListener(this::onClick);
        button_info.setOnClickListener(this::onClick);

        chbLoop.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mediaPlayer != null)
                mediaPlayer.setLooping(isChecked);
        });
        return view;
    }

    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher, as an instance variable.
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                        for (String permission : result.keySet()) {
                            switch (permission) {
                                case Manifest.permission.READ_EXTERNAL_STORAGE: {
                                    checkStoragePermission(Objects.requireNonNull(result.get(permission)));
                                    break;
                                }
                                case Manifest.permission.CAMERA:
                                    break;
                            }
                        }
                    }
            );

    private void checkStoragePermission(Boolean isGranted) {
        if (isGranted) {
            DATA_URI = Uri.parse(getMusicUri(music_num));
            startUri();
        } else if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            if (isshowdialog) isshowdialog = false;
            else {
                DialogFrag myDialogFragment = DialogFrag.newInstance();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    FragmentManager manager = activity.getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    myDialogFragment.show(transaction, "dialog");
                }
            }
        } else {
            Log.d(LOG_TAG, "Denied");
            isshowdialog = true;
            //запрос отклонён
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            int hasCameraPermission = requireContext().checkSelfPermission(Manifest.permission.CAMERA);
            if (hasReadPermission == PackageManager.PERMISSION_DENIED || hasCameraPermission == PackageManager.PERMISSION_DENIED) {//разрешение не дано
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});//запросить разрешение, в лаунчере словим ответ
            } else {//разрешение дано
                DATA_URI = Uri.parse(getMusicUri(music_num));
                startUri();
                //you can start camera
            }
        }
    }


    private final ActivityResultLauncher<Intent> startActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        // Do your code from onActivityResult
        Context context = getContext();
        if (context != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                DATA_URI = Uri.parse(getMusicUri(music_num));
                startUri();
            }
    });

    public String getMusicUri(int music_num) {
        if (getActivity() == null) return "";
        ContentResolver contentResolver = getActivity().getContentResolver();
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        ArrayList<String> list = new ArrayList<>();

        if (cursor == null) {
            //error
            return "";
        } else if (!cursor.moveToFirst()) {
            //no media
            return "";
        } else {
            int idColumn = cursor
                    .getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            do {
                long thisId = cursor.getLong(idColumn);
                list.add(uri + "/" + thisId);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list.get(music_num);
    }


    public void launch() {
        if (getContext() != null) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
            startActivityLauncher.launch(intent);
        }
    }

    public void onClickStart(View view) {
        releaseMP();

        try {
            final int viewId = view.getId();
            if (viewId == R.id.btnStartHttp) {
                Log.d(LOG_TAG, "start HTTP");
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(DATA_HTTP);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                Log.d(LOG_TAG, "prepareAsync");
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync();
            } else if (viewId == R.id.btnStartUri) {
                Log.d(LOG_TAG, "start Uri");
                requestPermissions();
            } else if (viewId == R.id.btnStartRaw) {
                Log.d(LOG_TAG, "start Raw");
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.linkin_park);
                if (requestAudioFoc())
                    mediaPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mediaPlayer == null)
            return;

        mediaPlayer.setLooping(chbLoop.isChecked());
        mediaPlayer.setOnCompletionListener(this);
    }

    private void startUri() {
        if (DATA_URI.equals(Uri.parse(""))) {
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getContext(), DATA_URI);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (requestAudioFoc())
            mediaPlayer.start();
    }

    private boolean requestAudioFoc() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            afListenerMusic = new AFListener(mediaPlayer, "Music");
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .setAudioAttributes(playbackAttributes)
                    .setOnAudioFocusChangeListener(afListenerMusic)
                    .build();

            int res = am.requestAudioFocus(focusRequest);
            return res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            afListenerMusic = new AFListener(mediaPlayer, "Music");
            int requestResult = am.requestAudioFocus(afListenerMusic,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            Log.d(LOG_TAG, "Music request focus, result: " + requestResult);
            return requestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void abandonFoc() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            am.abandonAudioFocusRequest(focusRequest);
        } else {
            am.abandonAudioFocus(afListenerMusic);
        }
    }

    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (afListenerMusic != null)
                abandonFoc();
        }
    }

    public void onClick(View view) {
        if (mediaPlayer == null)
            return;
        final int viewId = view.getId();
        if (viewId == R.id.btnPause) {
            if (mediaPlayer.isPlaying())
                abandonFoc();
            mediaPlayer.pause();
        } else if (viewId == R.id.btnResume) {
            if (!mediaPlayer.isPlaying())
                afListenerMusic = new AFListener(mediaPlayer, "Music");
            int requestResult = am.requestAudioFocus(afListenerMusic,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            Log.d(LOG_TAG, "Music request focus, result: " + requestResult);
            mediaPlayer.start();
        } else if (viewId == R.id.btnStop) {
            abandonFoc();
            mediaPlayer.stop();
        } else if (viewId == R.id.btnBackward) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 3000);
        } else if (viewId == R.id.btnForward) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 3000);
        } else if (viewId == R.id.btnInfo) {
            Log.d(LOG_TAG, "Playing " + mediaPlayer.isPlaying());
            Log.d(LOG_TAG, "Time " + mediaPlayer.getCurrentPosition() + " / "
                    + mediaPlayer.getDuration());
            Log.d(LOG_TAG, "Looping " + mediaPlayer.isLooping());
            Log.d(LOG_TAG,
                    "Volume " + am.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        abandonFoc();
        Log.d(LOG_TAG, "onCompletion");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(LOG_TAG, "onPrepared");
        if (requestAudioFoc())
            mp.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMP();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
}