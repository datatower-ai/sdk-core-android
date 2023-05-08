package com.roiquery.analytics_demo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.roiquery.analytics_demo.R
import com.roiquery.analytics_demo.databinding.MainActivityBinding
import com.roiquery.analytics_demo.ui.fn.core.DtSdkCoreFnFragment

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // TODO: Track fragment switches.
        supportFragmentManager?.commit {
            setReorderingAllowed(true)
            add<DtSdkCoreFnFragment>(R.id.fragment_container)
        }
    }
}
