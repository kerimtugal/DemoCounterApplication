package com.csystem.app.android.app.counter

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import org.csystem.app.android.app.counter.R
import org.csystem.app.android.app.counter.databinding.ActivityConfigureBinding
import java.lang.NumberFormatException
import java.util.concurrent.ExecutorService
import javax.inject.Inject

@AndroidEntryPoint
class LimitConfigurationActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityConfigureBinding

    @Inject
    @Named("threadPool")
    lateinit var threadPool: ExecutorService

    @Inject
    lateinit var conterDataService: CounterDataService

    private fun saveCallback(limit: Int) {
        try {
            conterDataService.setLimit(limit)
            finish()
        } catch (_: NumberFormatException) {
            runOnUiThread { Toast.makeText(this, R.string.message_invalid_value, Toast.LENGTH_LONG).show() }
        }
    }

    private fun initBinding() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_configure)
        mBinding.activity = this
    }

    private fun initialize() {
        enableEdgeToEdge()
        initBinding()
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    fun onSaveButtonClicked() {
       /* val limit = mBinding.limit?.toInt()
        Intent(this, MainActivity::class.java).apply {
            putExtra("limit", limit); startActivity(this) }
        */
        threadPool.execute { saveCallback(mBinding.limitValue!!.toInt())}
    }

    fun onNoLimitButtonClicked() = threadPool.execute {saveCallback(-1)}

    fun onCloseButtonClicked() = finish()

}