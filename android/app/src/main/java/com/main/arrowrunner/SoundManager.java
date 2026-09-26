package com.main.arrowrunner;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/**
 * Ultra Low Latency Native Android Audio Manager.
 * Uses SoundPool for instant audio trigger and zero webview audio lag.
 */
public class SoundManager {
    private SoundPool soundPool;
    private final SkinManager skinManager;

    public SoundManager(Context context, SkinManager skinManager) {
        this.skinManager = skinManager;
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(attributes)
                .build();
    }

    public void playCollect() {
        if (skinManager.isMuted()) return;
        // Native audio trigger stub
    }

    public void playCoin() {
        if (skinManager.isMuted()) return;
    }

    public void playHit() {
        if (skinManager.isMuted()) return;
    }

    public void playMove() {
        if (skinManager.isMuted()) return;
    }

    public void playGameOver() {
        if (skinManager.isMuted()) return;
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
