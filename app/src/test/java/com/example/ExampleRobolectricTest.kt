package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PlacementOrder
import com.example.model.TextRule
import com.example.util.GridGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Lottery Grid", appName)
  }

  @Test
  fun `test grid generation success`() {
    val rules = listOf(
      TextRule(text = "جعلی نوٹ", count = 15, order = PlacementOrder.RANDOM),
      TextRule(text = "غبارے", count = 10, order = PlacementOrder.RANDOM)
    )
    val result = GridGenerator.generate(
      rows = 15,
      cols = 6,
      totalNumbers = 20,
      numOrder = PlacementOrder.RANDOM,
      rules = rules
    )

    assertTrue(result is GridGenerator.GenerationResult.Success)
    val success = result as GridGenerator.GenerationResult.Success
    assertEquals(90, success.grid.stats.totalCells)
    assertEquals(45, success.grid.stats.filledCells)
    assertEquals(45, success.grid.stats.emptyCells)
  }

  @Test
  fun `test grid generation overflow error`() {
    val rules = listOf(
      TextRule(text = "جعلی نوٹ", count = 50, order = PlacementOrder.RANDOM)
    )
    val result = GridGenerator.generate(
      rows = 5,
      cols = 5,
      totalNumbers = 20,
      numOrder = PlacementOrder.RANDOM,
      rules = rules
    )

    assertTrue(result is GridGenerator.GenerationResult.Error)
  }

  @Test
  fun `test grid pdf creation safe execution`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = GridGenerator.generate(
      rows = 5,
      cols = 5,
      totalNumbers = 10,
      numOrder = PlacementOrder.RANDOM,
      rules = emptyList()
    )
    val success = result as GridGenerator.GenerationResult.Success
    // In Robolectric host JVM, native PdfDocument startPage throws without Android native PDF engine,
    // so createGridPdf safely catches and returns null without crashing the app.
    val pdfUri = GridGenerator.createGridPdf(context, success.grid)
    // Passes without throwing uncaught exception
  }
}

