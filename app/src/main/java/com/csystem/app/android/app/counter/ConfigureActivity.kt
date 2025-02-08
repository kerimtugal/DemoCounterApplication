package com.csystem.app.android.app.counter

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.csystem.app.android.app.counter.data.service.CounterDataService
import dagger.hilt.android.AndroidEntryPoint
import org.csystem.app.android.app.counter.R
import org.csystem.app.android.app.counter.databinding.ActivityConfigureBinding
import javax.inject.Inject

@AndroidEntryPoint
class ConfigureActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityConfigureBinding


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
        val limit = mBinding.limit?.toInt()
        Intent(this, MainActivity::class.java).apply {
            putExtra("limit", limit); startActivity(this) }
    }
}