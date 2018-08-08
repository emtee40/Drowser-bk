package com.jarsilio.android.drowser

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.jarsilio.android.drowser.adapters.AppItemListAdapter
import com.jarsilio.android.drowser.models.AppDatabase
import com.jarsilio.android.drowser.models.AppItem
import com.jarsilio.android.drowser.models.AppsManager
import com.jarsilio.android.drowser.models.AppItemsViewModel
import com.jarsilio.android.drowser.prefs.Prefs

class AddActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var prefs: Prefs
    private lateinit var appsManager: AppsManager

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var appItemListAdapter: AppItemListAdapter
    private lateinit var swipeLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = Prefs(this)
        appsManager = AppsManager(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view) as RecyclerView

        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        appItemListAdapter = AppItemListAdapter()
        recyclerView.adapter = appItemListAdapter

        val viewModel = ViewModelProviders.of(this).get(AppItemsViewModel::class.java)
        viewModel.getUserApps(AppDatabase.getInstance(applicationContext).appItemsDao())
            .observe(this,
                Observer<List<AppItem>> { list ->
                    appItemListAdapter.submitList(list)
                }
            )

        swipeLayout = findViewById(R.id.swipe_container)
        swipeLayout.setOnRefreshListener(this)

        onRefresh()
    }

    override fun onRefresh() {
        appsManager.updateAppItemsDatabase()
        swipeLayout.setRefreshing(false)
    }

    override fun onResume() {
        super.onResume()
        onRefresh()
    }

    override fun onRestart() {
        super.onRestart()
        onRefresh()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}