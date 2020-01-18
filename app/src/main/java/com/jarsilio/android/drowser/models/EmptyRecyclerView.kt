/*
 * Copyright (C) 2015 Glowworm Software
 * Copyright (C) 2014 Nizamutdinov Adel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// based on https://gist.github.com/adelnizamutdinov/31c8f054d1af4588dc5c

package com.jarsilio.android.drowser.models

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

class EmptyRecyclerView : RecyclerView {

    private var emptyView: View? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            checkIfEmptyAndToggleVisibility()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            checkIfEmptyAndToggleVisibility()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            checkIfEmptyAndToggleVisibility()
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)

        adapter?.registerAdapterDataObserver(observer)
        super.setAdapter(adapter)
        checkIfEmptyAndToggleVisibility()
    }

    override fun swapAdapter(adapter: Adapter<*>?, removeAndRecycleExistingViews: Boolean) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)

        adapter?.registerAdapterDataObserver(observer)
        super.swapAdapter(adapter, removeAndRecycleExistingViews)
        checkIfEmptyAndToggleVisibility()
    }

    /**
     * Indicates the view to be shown when the adapter for this object is empty
     *
     * @param view
     */
    fun setEmptyView(view: View?) {
        emptyView?.visibility = View.GONE

        emptyView = view
        checkIfEmptyAndToggleVisibility()
    }

    /**
     * Check adapter item count and toggle visibility of empty view if the adapter is empty
     */
    private fun checkIfEmptyAndToggleVisibility() {
        if (emptyView == null || adapter == null) {
            return
        }

        if (adapter!!.itemCount > 0) {
            emptyView!!.visibility = View.GONE
        } else {
            emptyView!!.visibility = View.VISIBLE
        }
    }
}