package com.jop.widget.component

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.jcodecraeer.xrecyclerview.LoadingMoreFooter
import com.jcodecraeer.xrecyclerview.XRecyclerView


/**
 * Created by CL on 5/1/18.
 */

class JRecyclerview @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : XRecyclerView(context, attrs, defStyleAttr) {
    private var parent: SwipeRefreshLayout? = null
    private lateinit var cacheListener: LoadingListener
    var manualControlLoadMore: Boolean = false
    var loadDoneHint = ""
    var loadingHint = ""

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (getParent() is SwipeRefreshLayout) {
            setPullRefreshEnabled(false)
            parent = getParent() as SwipeRefreshLayout?
            parent?.setOnChildScrollUpCallback { parent, child ->
                if (child is JRecyclerview) {
                    return@setOnChildScrollUpCallback !isOnTop(child)
                }
                false
            }
        }
        defaultFootView.apply {
            setLoadingDoneHint(loadDoneHint)
            setLoadingHint(loadingHint)
        }
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                parent?.let {
                    if (it.isRefreshing) {
                        setLoadingMoreEnabled(false)
                    } else if (!manualControlLoadMore) {
                        setLoadingMoreEnabled(true)
                    }
                }
            }
        })
    }

    override fun setLoadingMoreEnabled(enabled: Boolean) {
        if (footView is LoadingMoreFooter) {
            val loadMoreFoot = footView as LoadingMoreFooter
            if (enabled) {
                if (::cacheListener.isInitialized) {
                    setLoadingListener(cacheListener)
                }
            } else {
                loadMoreFoot.setState(LoadingMoreFooter.STATE_COMPLETE)
                setLoadingListener(null)
            }
        }
    }

    fun setLoadingMoreListener(loadMoreFunction: (() -> Unit)?) {
        object : LoadingListener {
            override fun onLoadMore() {
                if (footView.visibility == View.VISIBLE && footView.isAttachedToWindow && footView.top > 0) {
                    loadMoreFunction?.invoke()
                } else {
                    loadMoreComplete()
                }
            }

            override fun onRefresh() {
            }
        }.let {
            if (loadMoreFunction != null) {
                cacheListener = it
            }
            super.setLoadingListener(it)
        }

    }

    private fun isOnTop(rc: RecyclerView): Boolean {
        val lm = rc.layoutManager as LinearLayoutManager?
        if (lm?.findViewByPosition(lm.findFirstVisibleItemPosition())?.top == 0 && lm.findFirstVisibleItemPosition() == headers_includingRefreshCount) {
            return true
        }
        return false
    }

    fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
        adapter.notifyItemRangeInserted(headers_includingRefreshCount + positionStart, itemCount)
    }

    fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
        adapter.notifyItemRangeRemoved(headers_includingRefreshCount + positionStart, itemCount)
    }

    fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) {
        adapter.notifyItemRangeChanged(headers_includingRefreshCount + positionStart, itemCount)
    }
}
