package com.jarsilio.android.drowser

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
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
import com.jarsilio.android.drowser.services.Timeout
import com.jarsilio.android.drowser.utils.Utils
import com.jarsilio.android.privacypolicy.PrivacyPolicyBuilder
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import eu.chainfire.libsuperuser.Shell
import timber.log.Timber
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
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
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
        searchView.setOnQueryTextListener(this)
        return true
    }

    override fun onQueryTextChange(query: String): Boolean {
        // Here is where we are going to implement the filter logic
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
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
        val timeoutStrings = Timeout.getStringsForChoiceDialog(this)
        AlertDialog.Builder(this)
                .setTitle("Disable drowsing apps for")
                .setSingleChoiceItems(timeoutStrings, prefs.lastDisableUntilUserChoice) { dialog, which ->
                    prefs.lastDisableUntilUserChoice = which
                    prefs.disableUntil = Timeout.values()[which].disableUntil
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
        val snackbar = Snackbar.make(findViewById<View>(R.id.main_content),
                getString(R.string.snackbar_zzz),
                Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    Timber.d("User canceled 'zzz'. Not drowsing apps...")
                }
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(snackbar: Snackbar?, event: Int) {
                if (event == DISMISS_EVENT_ACTION) {
                    return
                }
                Timber.d("User didn't cancel 'zzz'. Drowsing apps...")
                AppsManager(applicationContext).forceStopApps()
            }
        })
        snackbar.show()
    }
}
