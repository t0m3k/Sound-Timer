package uk.aurorastudio.soundtimer

import android.media.MediaRecorder

class MicrophoneInput {

    private val EMA_FILTER : Double = 0.6
    private var mRecorder : MediaRecorder? = null
    private var mEMA : Double = 0.0

    fun start() {
        if (mRecorder == null) {
            mRecorder = MediaRecorder()
            mRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mRecorder?.setOutputFile("/dev/null")
            mRecorder?.prepare()
            mRecorder?.start()
            mEMA = 0.0
        }
    }

    fun stop() {
        if (mRecorder != null) {
            mRecorder?.stop()
            mRecorder?.release()
            mRecorder = null
        }
    }

    fun getAmplitude(): Double {
        return (mRecorder?.maxAmplitude ?: 0) / 2700.0
    }

    fun getAmplitudeEMA(): Double {
        val amp = getAmplitude()
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA
        return mEMA
    }

}