package com.example.game.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.game.model.ClubType
import com.example.game.model.PlayConsoleSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.sin

object GolfAudioHapticEngine {

    private const val MAX_CONCURRENT_TRACKS = 6
    private const val WHEEL_TICK_THROTTLE_MS = 35L

    private var vibrator: Vibrator? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val activeAudioTracks = AtomicInteger(0)
    private val lastWheelTickMs = AtomicLong(0L)

    @Volatile
    var isSoundEnabled: Boolean = true
        private set

    @Volatile
    var soundVolume: Float = 0.85f
        private set

    @Volatile
    var isHapticsEnabled: Boolean = true
        private set

    fun activeTrackCount(): Int = activeAudioTracks.get()

    fun applySettings(settings: PlayConsoleSettings) {
        isSoundEnabled = settings.isSoundEnabled
        soundVolume = settings.soundVolume.coerceIn(0f, 1f)
        isHapticsEnabled = settings.isHapticsEnabled
    }

    fun initialize(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.applicationContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playSwingImpactSound(club: ClubType, isApexSweetSpot: Boolean) {
        scope.launch {
            try {
                if (isApexSweetSpot) {
                    triggerHaptic(50)
                    playSynthesizedChime(freq1 = 880f, freq2 = 1320f, durationMs = 120)
                } else if (club.isPutter) {
                    triggerHaptic(20)
                    playSynthesizedTone(frequency = 520f, durationMs = 60, volume = 0.5f)
                } else if (club == ClubType.DRIVER) {
                    triggerHaptic(40)
                    playSynthesizedTone(frequency = 750f, durationMs = 90, volume = 0.9f)
                } else {
                    triggerHaptic(30)
                    playSynthesizedTone(frequency = 640f, durationMs = 70, volume = 0.7f)
                }
            } catch (_: Exception) {
                // Ignore audio hardware errors
            }
        }
    }

    fun playCupSinkSound() {
        scope.launch {
            try {
                triggerHapticPattern(longArrayOf(0, 40, 60, 80))
                playSynthesizedChime(freq1 = 659.25f, freq2 = 987.77f, durationMs = 250)
            } catch (_: Exception) {
                // Ignore audio hardware errors
            }
        }
    }

    fun playHazardSplashSound() {
        scope.launch {
            try {
                triggerHaptic(80)
                playSynthesizedNoise(durationMs = 200, lowPitch = true)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun playBunkerThudSound() {
        scope.launch {
            try {
                triggerHaptic(45)
                playSynthesizedTone(frequency = 180f, durationMs = 100, volume = 0.6f)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun playWheelTickSound() {
        val now = System.currentTimeMillis()
        val prev = lastWheelTickMs.get()
        if (now - prev < WHEEL_TICK_THROTTLE_MS || !lastWheelTickMs.compareAndSet(prev, now)) {
            return
        }
        scope.launch {
            try {
                triggerHaptic(12)
                playSynthesizedTone(frequency = 1100f, durationMs = 25, volume = 0.4f)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun playVictoryRewardSound() {
        scope.launch {
            try {
                triggerHapticPattern(longArrayOf(0, 30, 50, 40, 50, 80))
                playSynthesizedChime(freq1 = 784.0f, freq2 = 1046.5f, durationMs = 350)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun playBullseyeBellSound() {
        scope.launch {
            try {
                triggerHapticPattern(longArrayOf(0, 60, 40, 90))
                playSynthesizedChime(freq1 = 1318.5f, freq2 = 1760.0f, durationMs = 450)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    fun playFanfareCrowdSound() {
        scope.launch {
            try {
                triggerHapticPattern(longArrayOf(0, 40, 60, 40, 60, 100))
                playSynthesizedChime(freq1 = 523.25f, freq2 = 1046.5f, durationMs = 600)
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    private fun triggerHaptic(durationMs: Long) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun triggerHapticPattern(timings: LongArray) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }

    private fun playSynthesizedTone(frequency: Float, durationMs: Int, volume: Float) {
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        val effectiveVolume = (volume * soundVolume).coerceIn(0f, 1f)
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Decaying envelope
            val envelope = (1.0 - (i.toDouble() / numSamples))
            val sample = (sin(2.0 * Math.PI * frequency * t) * envelope * effectiveVolume * Short.MAX_VALUE).toInt().toShort()
            generatedSnd[i] = sample
        }

        playPcm(generatedSnd, sampleRate, durationMs)
    }

    private fun playSynthesizedChime(freq1: Float, freq2: Float, durationMs: Int) {
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        val effectiveVolume = (0.85 * soundVolume).coerceIn(0.0, 1.0)
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = (1.0 - (i.toDouble() / numSamples))
            val sample = ((sin(2.0 * Math.PI * freq1 * t) * 0.5 + sin(2.0 * Math.PI * freq2 * t) * 0.5) * envelope * effectiveVolume * Short.MAX_VALUE).toInt().toShort()
            generatedSnd[i] = sample
        }

        playPcm(generatedSnd, sampleRate, durationMs)
    }

    private fun playSynthesizedNoise(durationMs: Int, lowPitch: Boolean) {
        if (!isSoundEnabled || soundVolume <= 0.01f) return
        val effectiveVolume = (0.75 * soundVolume).coerceIn(0.0, 1.0)
        val sampleRate = 22050
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val generatedSnd = ShortArray(numSamples)

        var lastSample = 0.0
        for (i in 0 until numSamples) {
            val noise = (Math.random() * 2.0 - 1.0)
            // Low-pass filter for splash/thump
            val filtered = if (lowPitch) (lastSample * 0.85 + noise * 0.15) else noise
            lastSample = filtered
            val envelope = (1.0 - (i.toDouble() / numSamples))
            generatedSnd[i] = (filtered * envelope * effectiveVolume * Short.MAX_VALUE).toInt().toShort()
        }

        playPcm(generatedSnd, sampleRate, durationMs)
    }

    private fun playPcm(buffer: ShortArray, sampleRate: Int, durationMs: Int) {
        if (activeAudioTracks.get() >= MAX_CONCURRENT_TRACKS) return
        var audioTrack: AudioTrack? = null
        val released = AtomicBoolean(false)
        fun safeRelease() {
            if (released.compareAndSet(false, true)) {
                try {
                    audioTrack?.stop()
                } catch (_: Exception) {}
                try {
                    audioTrack?.release()
                } catch (_: Exception) {}
                activeAudioTracks.updateAndGet { c -> (c - 1).coerceAtLeast(0) }
            }
        }

        try {
            activeAudioTracks.incrementAndGet()
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes((buffer.size * 2).coerceAtLeast(256))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.setNotificationMarkerPosition(buffer.size)
            audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(track: AudioTrack?) {
                    safeRelease()
                }
                override fun onPeriodicNotification(track: AudioTrack?) {}
            })
            audioTrack.play()

            // Guaranteed cleanup fallback in case hardware marker callback is skipped
            scope.launch {
                delay(durationMs.toLong() + 120L)
                safeRelease()
            }
        } catch (_: Exception) {
            safeRelease()
        }
    }
}
