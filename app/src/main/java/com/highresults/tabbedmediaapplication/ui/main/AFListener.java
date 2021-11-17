package com.highresults.tabbedmediaapplication.ui.main;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

public class AFListener implements AudioManager.OnAudioFocusChangeListener {

    String label;
    MediaPlayer mp;

    public AFListener(MediaPlayer mp, String label) {
        this.label = label;
        this.mp = mp;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        String event = "";
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                event = "AUDIOFOCUS_LOSS";
                if (mp != null) {
                    try {
                        mp.release();
                        mp = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                event = "AUDIOFOCUS_LOSS_TRANSIENT";
                mp.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                event = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                mp.setVolume(0.5f, 0.5f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                event = "AUDIOFOCUS_GAIN";
                if (!mp.isPlaying())
                    mp.start();
                mp.setVolume(1.0f, 1.0f);
                break;
        }
        Log.d("AFLog", label + " onAudioFocusChange: " + event);
    }
}

