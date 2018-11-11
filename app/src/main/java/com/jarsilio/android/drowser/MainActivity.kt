package com.jarsilio.android.drowser

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.jarsilio.android.drowser.adapters.AppItemListAdapter
import com.jarsilio.android.drowser.models.AppDatabase
import com.jarsilio.android.drowser.models.AppItem
import com.jarsilio.android.drowser.models.AppItemsDao
import com.jarsilio.android.drowser.models.AppItemsViewModel
import com.jarsilio.android.drowser.models.AppsManager
import com.jarsilio.android.drowser.models.EmptyRecyclerView
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.services.DrowserService
import com.jarsilio.android.drowser.utils.Utils
import com.jarsilio.android.privacypolicy.PrivacyPolicyBuilder
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import eu.chainfire.libsuperuser.Shell
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: Prefs
    private lateinit var appsManager: AppsManager
    private lateinit var appItemsDao: AppItemsDao

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs.getInstance(this)
        appsManager = AppsManager(this)
        appItemsDao = AppDatabase.getInstance(this).appItemsDao()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)

        val drowseCandidatesRecyclerView = findViewById<EmptyRecyclerView>(R.id.recycler_drowse_candidates) as EmptyRecyclerView
        val emptyView = findViewById<CardView>(R.id.empty_view)
        drowseCandidatesRecyclerView.setEmptyView(emptyView)
        drowseCandidatesRecyclerView.layoutManager = LinearLayoutManager(this)
        val drowseCandidatesListAdapter = AppItemListAdapter()
        drowseCandidatesRecyclerView.adapter = drowseCandidatesListAdapter

        val nonDrowseCandidatesRecyclerView = findViewById<RecyclerView>(R.id.recycler_non_drowse_candidates) as RecyclerView
        nonDrowseCandidatesRecyclerView.layoutManager = LinearLayoutManager(this)
        val nonDrowseCandidatesListAdapter = AppItemListAdapter()
        nonDrowseCandidatesRecyclerView.adapter = nonDrowseCandidatesListAdapter

        val viewModel = ViewModelProviders.of(this).get(AppItemsViewModel::class.java)
        viewModel.getDrowseCandidates(appItemsDao)
                .observe(this,
                        Observer<List<AppItem>> { list ->
                            drowseCandidatesListAdapter.submitList(list)
                        }
                )
        viewModel.getNonDrowseCandidates(appItemsDao).observe(this,
                Observer<List<AppItem>> { list ->
                    nonDrowseCandidatesListAdapter.submitList(list)
                }
        )

        // This enables inertia while scrolling
        drowseCandidatesRecyclerView.isNestedScrollingEnabled = false
        nonDrowseCandidatesRecyclerView.isNestedScrollingEnabled = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (prefs.disableUntil < System.currentTimeMillis()) {
            menu?.findItem(R.id.menu_item_global_pause)?.isVisible = true
            menu?.findItem(R.id.menu_item_global_play)?.isVisible = false
        } else {
            menu?.findItem(R.id.menu_item_global_pause)?.isVisible = false
            menu?.findItem(R.id.menu_item_global_play)?.isVisible = true
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_settings -> startActivity(Intent(this, PreferencesActivity::class.java))
            R.id.menu_item_privacy_policy -> showPrivacyPolicyActivity()
            R.id.menu_item_licenses -> showAboutLicensesActivity()
            R.id.menu_item_global_pause -> showDisableUntilDialog()
            R.id.menu_item_global_play -> reEnable()
            R.id.menu_item_drowse_now -> drowseNow()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
        requestRootAccessIfNecessaryAndStartService()
    }

    fun requestRootAccessIfNecessaryAndStartService() {
        if (prefs.requestRootAccess && !Shell.SU.available()) {
            Timber.e("Root access denied!")
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.root_required))
                    .setPositiveButton(android.R.string.yes, { dialog, which -> finish() })
                    .setCancelable(false)
                    .show()
        } else {
            appsManager.updateAppItemsDatabase()
            prefs.requestRootAccess = false
            DrowserService.startService(this)
        }
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

    private fun showDisableUntilDialog() {
        val timeoutStrings = arrayOf("1 minute", "5 minutes", "30 minutes", "1 hour", "2 hours", "indefinitely")
        AlertDialog.Builder(this)
                .setTitle("Disable drowsing apps for")
                .setSingleChoiceItems(timeoutStrings, prefs.lastDisableUntilUserChoide) { dialog, which ->
                    prefs.lastDisableUntilUserChoide = which
                    prefs.disableUntil = when (which) {
                        0 -> System.currentTimeMillis() + 1 * 60 * 1000
                        1 -> System.currentTimeMillis() + 5 * 60 * 1000
                        2 -> System.currentTimeMillis() + 30 * 60 * 1000
                        3 -> System.currentTimeMillis() + 1 * 60 * 60 * 1000
                        4 -> System.currentTimeMillis() + 2 * 60 * 60 * 1000
                        5 -> Long.MAX_VALUE // It's the year 292,278,994 :) Will Drowser or mankind exist?
                        else -> 0
                    }
                    Timber.d("Temporarily disabling Drowser until ${Utils.getReadableDate(prefs.disableUntil)}")
                    Snackbar.make(findViewById<View>(R.id.main_content),
                            getString(R.string.snackbar_disabled_until, Utils.getReadableTime(prefs.disableUntil)),
                            Snackbar.LENGTH_LONG).show()
                    dialog.dismiss()
                    invalidateOptionsMenu() // force onPrepareOptionsMenu
                }
                .setNegativeButton(android.R.string.no) { dialog, which -> }
                .show()
    }

    private fun reEnable() {
        prefs.disableUntil = 0
        invalidateOptionsMenu() // force onPrepareOptionsMenu
        Snackbar.make(findViewById<View>(R.id.main_content),
                getString(R.string.snackbar_reenabled),
                Snackbar.LENGTH_LONG).show()
    }

    private fun drowseNow() {
        Snackbar.make(findViewById<View>(R.id.main_content),
                getString(R.string.snackbar_zzz),
                Snackbar.LENGTH_LONG).show()
        AppsManager(this).forceStopApps()
    }
}
