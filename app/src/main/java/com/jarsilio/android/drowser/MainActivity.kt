package com.jarsilio.android.drowser

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.jarsilio.android.drowser.adapters.PageAdapter
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.services.DrowserService
import timber.log.Timber



class MainActivity : AppCompatActivity() {
    private var prefs: Prefs? = null
    private var mSectionsPagerAdapter: PageAdapter? = null

    /**
     * The [ViewPager] that will host the section contents.
     */
    private var mViewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = PageAdapter(supportFragmentManager, this)

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById<ViewPager?>(R.id.container)
        mViewPager!!.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)

        startService()
    }

    private fun startService() {
        if (prefs!!.isEnabled) {
            Timber.i("Starting Drowser Service")
            startService(Intent(this, DrowserService::class.java))
        } else {
            Timber.i("Stopping Drowser Service")
            stopService(Intent(this, DrowserService::class.java))
        }
    }
}