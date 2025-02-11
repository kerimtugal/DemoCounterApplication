package com.csystem.app.android.app.counter

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.csystem.app.android.app.counter.data.service.CounterDataService
import com.csystem.app.android.app.model.SecondModel
import dagger.hilt.android.AndroidEntryPoint
import org.csystem.android.library.util.datetime.module.annotation.DateTimeFormatterTRInterceptor
import org.csystem.app.android.app.counter.R
import org.csystem.app.android.app.counter.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import kotlin.random.Random


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private var mStartedFlag = false
    private var mSeconds: Long = 0L
    private var mCounterScheduledFuture: ScheduledFuture<*>? = null
    private lateinit var mDateTimeScheduledFuture: ScheduledFuture<*>
    lateinit var mProgressBarCircle: ProgressBar

    @Inject
    @Named("scheduledExecutorService")
    lateinit var counterScheduledThreadPool: ScheduledExecutorService

    @Inject
    @Named("scheduledExecutorService")
    lateinit var dateTimeScheduledThreadPool: ScheduledExecutorService

    @Inject
    @Named("threadPool")
    lateinit var threadPool: ExecutorService

    @Inject
    @DateTimeFormatterTRInterceptor
    lateinit var dateTimeFormatter: DateTimeFormatter

    @Inject
    lateinit var counterDataService: CounterDataService

    private fun loadAllSecondsThreadCallback() {
        val secondsList = counterDataService.findAll().map {
            SecondModel(it[0].toLong(), it[1])
        }

        mBinding.secondsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList(secondsList))

    }

    private fun dateTimeSchedulerCallback() {
        val now = LocalDateTime.now()

        mBinding.dateTimeText = dateTimeFormatter.format(now)
    }

    private fun showAlertForReset() {
        AlertDialog.Builder(this)
            .setTitle(R.string.alert_dialog_reset_title)
            .setMessage(R.string.alert_dialog_reset_message)
            .setPositiveButton(R.string.alert_dialog_reset_positive_button_text) { _, _ -> threadPool.execute { } }
            .show()
    }

    private fun removeAllButtonClickedCallback() {
        threadPool.execute { counterDataService.removeAll() }
        mBinding.secondsAdapter?.clear()
    }

    private fun resetCallback() {
        if (!counterDataService.saveSeconds(if(mSeconds == 0L) mSeconds else mSeconds - 1 )) {
            runOnUiThread { showAlertForReset() }
            return
        }
        mSeconds = 0
        mBinding.counterText = "0:0:0"
        runOnUiThread { mBinding.mainActivityTextViewCounter.text = mBinding.counterText }
    }

    private fun startDateTimeScheduler() {
        mDateTimeScheduledFuture = dateTimeScheduledThreadPool.scheduleWithFixedDelay({dateTimeSchedulerCallback()}, 0L, 1L, TimeUnit.SECONDS)
    }

    private fun updateCircularTimer() {

        runOnUiThread {
            val colors = arrayOf(
                Color.parseColor("#9C27B0"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#AA00FF")
            )
            val colorIndex = Random.nextInt(1, 5)
            mProgressBarCircle.progressTintList = ColorStateList.valueOf(colors[colorIndex])
        }
    }

    private fun updateFlipClock(hour: Long, minute: Long, second: Long) {
        runOnUiThread {
            mBinding.hoursView.setValue(hour.toInt())
            mBinding.minutesView.setValue(minute.toInt())
            mBinding.secondsView.setValue(second.toInt())
        }
    }

    private fun schedulerCallback() {
        val hour = mSeconds / 60 / 60
        val minute = mSeconds / 60 % 60
        val second = mSeconds % 60

        setCounterText1(hour, minute, second)
        setCounterText2(hour, minute, second)
        updateCircularTimer()
        updateFlipClock(hour, minute, second)
        ++mSeconds
    }

    private fun setCounterText1(hour: Long, minute: Long, second: Long) {
        runOnUiThread {
            String.format("$hour:$minute:$second").apply {
                mBinding.mainActivityTextViewCounter.text = this
            }
        }
    }

    private fun setCounterText2(hour: Long, minute: Long, second: Long) {
        String.format("$hour:$minute:$second").apply {
            mBinding.counterText = this
        }
    }

    private fun setLimit() {
        intent.getIntExtra("limit", -1).also{ counterDataService.setLimit(it) }
    }

    private fun initialize() {
        enableEdgeToEdge()
        initBinding()

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        startDateTimeScheduler()
        setLimit()
    }

    private fun initBinding() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.activity = this
        mBinding.startStopButtonText = resources.getString(R.string.start_text)
        mBinding.counterText = "0:0:0"
        mBinding.dateTimeText = ""
        mBinding.secondsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())

        mProgressBarCircle = findViewById(R.id.progressBarCircle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mCounterScheduledFuture != null)
            mCounterScheduledFuture?.cancel(false)

        mDateTimeScheduledFuture.cancel(false)
    }

    fun onConfigureButtonClicked() {
        Intent(this, LimitConfigurationActivity::class.java).apply { startActivity(this) }
    }

    fun onStartStopButtonClicked() {
        if (mStartedFlag) {
            mBinding.startStopButtonText = resources.getString(R.string.start_text)
            mCounterScheduledFuture?.cancel(false)
            mCounterScheduledFuture = null
        } else {
            mCounterScheduledFuture = counterScheduledThreadPool.scheduleWithFixedDelay({ schedulerCallback() }, 0, 1, TimeUnit.SECONDS)
            mBinding.startStopButtonText = resources.getString(R.string.stop_text)
        }

        mStartedFlag = !mStartedFlag
    }

    fun onLoadButtonClicked() {
        if (mStartedFlag) {
            mBinding.startStopButtonText = resources.getString(R.string.start_text)
            mCounterScheduledFuture?.cancel(false)
            mCounterScheduledFuture = null
        }

        mSeconds = mBinding.secondSelected ?: return
        mStartedFlag = false
        val hour = mSeconds / 60 / 60
        val minute = mSeconds / 60 % 60
        val second = mSeconds % 60

        setCounterText1(hour, minute, second)
        setCounterText2(hour, minute, second)
        updateCircularTimer()
        updateFlipClock(hour, minute, second)

    }

    fun onLoadAllButtonClicked() {
        threadPool.execute { loadAllSecondsThreadCallback() }
    }

    fun onResetButtonClicked() {
        threadPool.execute { resetCallback() }
    }

    fun onRemoveAllButtonClicked() {
        AlertDialog.Builder(this)
            .setTitle(R.string.alert_dialog_remove_all_title)
            .setMessage(R.string.alert_dialog_remove_all_message)
            .setPositiveButton(R.string.alert_dialog_remove_all_positive_button_text) {_, _ -> removeAllButtonClickedCallback() }
            .setNegativeButton(R.string.alert_dialog_remove_all_negative_button_text) {_, _ -> }
            .show()

    }

    fun onSecondSelected(pos: Int) {
        mBinding.secondSelected = mBinding.secondsAdapter?.getItem(pos)?.seconds
    }

}