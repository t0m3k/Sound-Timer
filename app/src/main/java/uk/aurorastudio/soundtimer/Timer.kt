package uk.aurorastudio.soundtimer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_timer.*
import kotlin.concurrent.fixedRateTimer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v4.app.ActivityCompat

import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.util.Timer

class Timer : AppCompatActivity(), OnSeekBarChangeListener {

    enum class TimerState{
        Stopped, Running, Paused
    }

    private var recordPermission = false
    private val RECORD_REQUEST_CODE = 101

    private var timerLength = 0L
    private var timerState = TimerState.Stopped
    private var amp : Double = 0.0
    private var ampSetting : Int = 100
    private var dataPointsStored : Int = 15

    private var timer : Timer? = null

    private var ampData : MutableList<Double> = MutableList(0) {0.0}

    private var graphData : LineGraphSeries<DataPoint> = LineGraphSeries(arrayOf(DataPoint(0.0, 0.0), DataPoint(12.0, 0.0)))


    private var mSensor : MicrophoneInput = MicrophoneInput()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        seekBar!!.setOnSeekBarChangeListener(this)

        seekBar.progress = ampSetting


        graph.addSeries(graphData)

        timerViewRefresh()

        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMaxY(12.0)
        graph.viewport.setMinY(0.0)
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(dataPointsStored.toDouble())

        setupPermissions()
        if (recordPermission) {
            startMeter()
        } else {
            setupPermissions()
        }

    }



    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        ampSetting = progress
        meterTV.text = (ampSetting / 10.0).toString()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // called when tracking the seekbar is started
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // called when tracking the seekbar is stopped
    }

    private fun startMeter(){
        mSensor.start()
        var meter = fixedRateTimer("meter_timer", false, 0L, 200L) {
            meterMeasure()
            ampDataToGraph()
            if((ampData.subList(ampData.lastIndex / 2, ampData.lastIndex).average() > ampSetting / 10.0) && timerState == TimerState.Stopped)
                startTimer()
            else if ((ampData.average() < ampSetting / 13.0) && timerState == TimerState.Running)
                stopTimer()
        }
    }

    private fun startTimer(){
        if(timerState == TimerState.Stopped && timer == null){
            timerLength = 0
            timer = fixedRateTimer("clock_timer", false, 0L, 1000L) {
                if (timerState == TimerState.Running)
                    counterAdd()
            }
        }
        timerState = TimerState.Running
    }

    private fun stopTimer() {
        if (timerState == TimerState.Running && timer != null) {
            timerState = TimerState.Stopped
            timer?.cancel()
            timer = null
            timerViewRefresh()
        }
    }

    private fun pauseTimer() {
            timerState = TimerState.Paused
    }

    @SuppressLint("SetTextI18n")
    private fun timerViewRefresh() {
        val minutesPassed = timerLength / 60
        val secondsPassed = timerLength - minutesPassed * 60
        val secondsString = secondsPassed.toString()

        this@Timer.runOnUiThread{
            timerTV.text = "$minutesPassed:${
            if (secondsString.length == 2) secondsString
            else "0$secondsString"}"
        }
    }


    private fun meterMeasure(){
        amp = mSensor.getAmplitudeEMA()
        if(ampData.lastIndex >= dataPointsStored)
            ampData.removeAt(0)
        ampData.add(amp)
    }

    /*
    * Add one second, update timer text view
    * */
    private fun counterAdd(){
        timerLength++
        timerViewRefresh()
    }

    /*
    * Get permission info, if denied run makeRequest
    * */
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)

        if (permission == PackageManager.PERMISSION_GRANTED) {
            recordPermission = true
        } else
            makeRequest()
    }

    /*
    * Make permission request
    * */
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                             permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RECORD_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    recordPermission = true
                } else {
                    setupPermissions()
                }
            }
        }
    }
    /*
    * Show data from ampData on graph
    * TODO: Add lines for average sound and threshold setting
    * */
    private fun ampDataToGraph() {
        val tmpArray = Array(dataPointsStored+1) {0.0}

        ampData.forEachIndexed { i, d -> tmpArray[i] = d }

        graphData.resetData(Array(dataPointsStored+1) { i -> DataPoint(i.toDouble(), tmpArray[i]) })
    }
}


