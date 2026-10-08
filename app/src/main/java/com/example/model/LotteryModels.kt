package com.example.model

import java.util.UUID

enum class PlacementOrder(val value: String, val urduLabel: String, val enLabel: String) {
    RANDOM("random", "بکھرے ہوئے", "Random"),
    SEQUENTIAL("sequential", "ترتیب سے", "Sequential");

    fun displayLabel(): String = "$enLabel ($urduLabel)"
}

data class TextRule(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val count: Int,
    val order: PlacementOrder = PlacementOrder.RANDOM
)

data class GridStatistics(
    val totalCells: Int = 0,
    val filledCells: Int = 0,
    val emptyCells: Int = 0
)

data class GeneratedGrid(
    val rows: Int,
    val cols: Int,
    val cells: List<String?>,
    val stats: GridStatistics
)
