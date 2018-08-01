package com.jarsilio.android.drowser

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.jarsilio.android.drowser.adapters.AppAdapter
import com.jarsilio.android.drowser.models.AppsManager
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.services.DrowserService
import com.jarsilio.android.privacypolicy.PrivacyPolicyBuilder
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var prefs: Prefs
    private lateinit var appsManager: AppsManager

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs(this)
        appsManager = AppsManager(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)

        recyclerView = findViewById(R.id.recycler_view)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = AppAdapter(appsManager.getDrowseCandidates())

        swipeLayout = findViewById(R.id.swipe_container)
        swipeLayout.setOnRefreshListener(this)

        DrowserService.startService(this)

        onRefresh()
    }

    override fun onResume() {
        super.onResume()
        onRefresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_ellipsys, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_button_add-> startActivity(Intent(this, AddActivity::class.java))
            R.id.menu_item_settings -> startActivity(Intent(this, PreferencesActivity::class.java))
            R.id.menu_item_privacy_policy -> showPrivacyPolicyActivity()
            R.id.menu_item_licenses -> showAboutLicensesActivity()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRefresh() {
        recyclerView.adapter = AppAdapter(appsManager.getDrowseCandidates())
        swipeLayout.setRefreshing(false)
    }

    private fun showPrivacyPolicyActivity() {
        val privacyPolicyBuilder = PrivacyPolicyBuilder()
                .withIntro(getString(R.string.app_name), "Juan García Basilio (juanitobananas)")
                .withUrl("https://gitlab.com/juanitobananas/drowser/blob/master/PRIVACY.md#drowser-privacy-policy")
                .withMeSection()
                .withEmailSection("juam+drowser@posteo.net")
                .withAutoGoogleOrFDroidSection()
        privacyPolicyBuilder.start(this)
    }

    private fun showAboutLicensesActivity() {
        LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withActivityTitle(getString(R.string.menu_item_licenses))
                .withAboutDescription(getString(R.string.licenses_about_libraries_text, getString(R.string.app_name)))
                .start(applicationContext)
    }
}