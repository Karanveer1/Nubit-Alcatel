package com.mobi.nubitalcatel.utils

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView


class ViewPagerRecyclerViewNubit : RecyclerView {
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() {
        setLayoutManager(ViewPagerLayoutManager(context, LinearLayoutManager.HORIZONTAL, false))

        // Add padding to show neighboring items
        setPadding(SIDE_PADDING, 0, SIDE_PADDING, 0)
        setClipToPadding(false)
        // Attach a SnapHelper for smooth snapping to center
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(this)

        // Add item decoration for spacing between items
        addItemDecoration(HorizontalItemDecoration(ITEM_SPACING))
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        // Check if layoutManager is of the correct type before casting

        if (layoutManager !is ViewPagerLayoutManager) {
            return super.fling(velocityX, velocityY)
        }

        val layoutManager = layoutManager as ViewPagerLayoutManager
        val targetPosition = layoutManager.findTargetPosition(velocityX)
        if (targetPosition == NO_POSITION) return super.fling(velocityX, velocityY)
        smoothScrollToPosition(targetPosition)
        return true
    }

    private class ViewPagerLayoutManager internal constructor(
        context: Context?,
        orientation: Int,
        reverseLayout: Boolean
    ) :
        LinearLayoutManager(context, orientation, reverseLayout) {
        override fun smoothScrollToPosition(
            recyclerView: RecyclerView,
            state: State,
            position: Int
        ) {
            val smoothScroller: LinearSmoothScroller =
                object : LinearSmoothScroller(recyclerView.context) {
                    override fun getHorizontalSnapPreference(): Int {
                        return SNAP_TO_START
                    }
                }
            smoothScroller.targetPosition = position
            startSmoothScroll(smoothScroller)
        }

        fun findTargetPosition(velocityX: Int): Int {
            if (velocityX > 0) {
                return findFirstVisibleItemPosition() + 1
            } else if (velocityX < 0) {
                return findFirstVisibleItemPosition()
            }
            return NO_POSITION
        }
    }

    // ItemDecoration for spacing between items
    private class HorizontalItemDecoration internal constructor(private val spacing: Int) :
        ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
            }
            outRect.left = spacing
            outRect.right = spacing
        }
    }

    companion object {
        private const val SIDE_PADDING = 60 // Adjust the side padding as needed
        private const val ITEM_SPACING = 10 // Spacing between items
    }
}

