package com.example.color_or_suit

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.preference.PreferenceManager

class SoundManager(private val context: Context) {

    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundType, Int>()

    enum class SoundType {
        CARD_FLIP,
        CORRECT,
        CORRECT_SUIT, // +5 poäng (jackpot-ljud)
        WRONG,
        WIN,
        LOSE
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        soundMap[SoundType.CARD_FLIP]    = soundPool.load(context, R.raw.card_flip, 1)
        soundMap[SoundType.CORRECT]      = soundPool.load(context, R.raw.correct, 1)
        soundMap[SoundType.CORRECT_SUIT] = soundPool.load(context, R.raw.correct_suit, 1)
        soundMap[SoundType.WRONG]        = soundPool.load(context, R.raw.wrong, 1)
        soundMap[SoundType.WIN]          = soundPool.load(context, R.raw.win, 1)
        soundMap[SoundType.LOSE]         = soundPool.load(context, R.raw.lose, 1)
    }

    private fun isSoundEnabled(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean("sound", true)
    }

    fun play(soundType: SoundType) {
        if (!isSoundEnabled()) return

        val soundId = soundMap[soundType] ?: return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
