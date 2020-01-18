package com.jarsilio.android.drowser

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.jarsilio.android.drowser.adapters.AppItemListAdapter
import com.jarsilio.android.drowser.models.AppDatabase
import com.jarsilio.android.drowser.models.AppItem
import com.jarsilio.android.drowser.models.AppItemsViewModel
import com.jarsilio.android.drowser.models.AppsManager
import com.jarsilio.android.drowser.models.EmptyRecyclerView
import com.jarsilio.android.drowser.prefs.Prefs
import com.jarsilio.android.drowser.services.DrowserService
import com.jarsilio.android.drowser.services.Timeout
import com.jarsilio.android.drowser.utils.Utils
import com.jarsilio.android.common.privacypolicy.PrivacyPolicyBuilder
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import eu.chainfire.libsuperuser.Shell
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs.getInstance(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)

        val drowseCandidatesRecyclerView = findViewById<EmptyRecyclerView>(R.id.recycler_drowse_candidates) as EmptyRecyclerView
        val emptyView = findViewById<androidx.cardview.widget.CardView>(R.id.empty_view)
        drowseCandidatesRecyclerView.setEmptyView(emptyView)
        drowseCandidatesRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val drowseCandidatesListAdapter = AppItemListAdapter()
        drowseCandidatesRecyclerView.adapter = drowseCandidatesListAdapter

        val nonDrowseCandidatesRecyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recycler_non_drowse_candidates) as androidx.recyclerview.widget.RecyclerView
        nonDrowseCandidatesRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        val nonDrowseCandidatesListAdapter = AppItemListAdapter()
        nonDrowseCandidatesRecyclerView.adapter = nonDrowseCandidatesListAdapter

        val appItemsDao = AppDatabase.getInstance(this).appItemsDao()
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
            AppsManager(this).updateAppItemsDatabase()
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
                    prefs.disableUntil = Timeout.values()[which + 1].disableUntil // To skip NO_TIMEOUT
                    Timber.d("Temporarily disabling Drowser until ${Utils.getReadableDate(prefs.disableUntil)}")
                    com.google.android.material.snackbar.Snackbar.make(findViewById<View>(R.id.main_content),
                            getString(R.string.snackbar_disabled_until, Utils.getReadableTime(prefs.disableUntil)),
                            com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
                    dialog.dismiss()
                    invalidateOptionsMenu() // force onPrepareOptionsMenu
                }
                .setNegativeButton(android.R.string.no) { dialog, which -> }
                .show()
    }

    private fun reEnable() {
        prefs.disableUntil = 0
        invalidateOptionsMenu() // force onPrepareOptionsMenu
        com.google.android.material.snackbar.Snackbar.make(findViewById<View>(R.id.main_content),
                getString(R.string.snackbar_reenabled),
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
    }

    private fun drowseNow() {
        val snackbar = com.google.android.material.snackbar.Snackbar.make(findViewById<View>(R.id.main_content),
                getString(R.string.snackbar_zzz),
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) {
                    Timber.d("User canceled 'zzz'. Not drowsing apps...")
                }
        snackbar.addCallback(object : com.google.android.material.snackbar.Snackbar.Callback() {
            override fun onDismissed(snackbar: com.google.android.material.snackbar.Snackbar?, event: Int) {
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
