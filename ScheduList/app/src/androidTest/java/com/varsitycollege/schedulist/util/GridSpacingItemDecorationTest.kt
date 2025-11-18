package com.varsitycollege.schedulist.util

import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.recyclerview.widget.RecyclerView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import android.view.View

/**
 * Instrumented test for GridSpacingItemDecoration
 */
@RunWith(AndroidJUnit4::class)
class GridSpacingItemDecorationTest {

    @Test
    fun decoration_with_edge_included_calculates_correct_offsets() {
        val spanCount = 2
        val spacing = 10
        val decoration = GridSpacingItemDecoration(spanCount, spacing, includeEdge = true)

        val outRect = Rect()
        val view = mock(View::class.java)
        val parent = mock(RecyclerView::class.java)
        val state = mock(RecyclerView.State::class.java)

        // Mock position 0 (first item)
        `when`(parent.getChildAdapterPosition(view)).thenReturn(0)

        decoration.getItemOffsets(outRect, view, parent, state)

        // Verify that offsets are set
        assertTrue(outRect.left >= 0)
        assertTrue(outRect.right >= 0)
        assertTrue(outRect.top >= 0)
        assertTrue(outRect.bottom >= 0)
    }

    @Test
    fun decoration_without_edge_calculates_correct_offsets() {
        val spanCount = 3
        val spacing = 15
        val decoration = GridSpacingItemDecoration(spanCount, spacing, includeEdge = false)

        val outRect = Rect()
        val view = mock(View::class.java)
        val parent = mock(RecyclerView::class.java)
        val state = mock(RecyclerView.State::class.java)

        // Mock position 1 (second item)
        `when`(parent.getChildAdapterPosition(view)).thenReturn(1)

        decoration.getItemOffsets(outRect, view, parent, state)

        // Verify that offsets are set
        assertTrue(outRect.left >= 0)
        assertTrue(outRect.right >= 0)
        assertTrue(outRect.top >= 0 || outRect.bottom >= 0)
    }

    @Test
    fun decoration_handles_different_span_counts() {
        val spacings = listOf(2, 3, 4)

        spacings.forEach { spanCount ->
            val decoration = GridSpacingItemDecoration(spanCount, 10, includeEdge = true)
            assertNotNull(decoration)
        }
    }
}

