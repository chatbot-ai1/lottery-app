package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.GeneratedGrid
import com.example.model.GridStatistics
import com.example.model.PlacementOrder
import com.example.model.TextRule
import com.example.util.GridGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LotteryUiState(
    val colsInput: String = "6",
    val rowsInput: String = "15",
    val totalNumbersInput: String = "20",
    val numOrder: PlacementOrder = PlacementOrder.RANDOM,
    val textRules: List<TextRule> = listOf(
        TextRule(text = "جعلی نوٹ", count = 15, order = PlacementOrder.RANDOM),
        TextRule(text = "غبارے", count = 10, order = PlacementOrder.RANDOM)
    ),
    val generatedGrid: GeneratedGrid? = null,
    val validationError: String? = null,
    val infoMessage: String? = null,
    val isExporting: Boolean = false,
    val isExportingPdf: Boolean = false
)

class LotteryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LotteryUiState())
    val uiState: StateFlow<LotteryUiState> = _uiState.asStateFlow()

    init {
        // Initial grid generation matching window.onload
        generateGrid()
    }

    fun onColsChanged(value: String) {
        _uiState.update { it.copy(colsInput = value) }
    }

    fun onRowsChanged(value: String) {
        _uiState.update { it.copy(rowsInput = value) }
    }

    fun onTotalNumbersChanged(value: String) {
        _uiState.update { it.copy(totalNumbersInput = value) }
    }

    fun onNumOrderChanged(order: PlacementOrder) {
        _uiState.update { it.copy(numOrder = order) }
    }

    fun addTextRule(defaultText: String = "", defaultCount: Int = 5) {
        _uiState.update { current ->
            current.copy(
                textRules = current.textRules + TextRule(
                    text = defaultText,
                    count = defaultCount,
                    order = PlacementOrder.RANDOM
                )
            )
        }
    }

    fun updateTextRule(id: String, text: String, count: Int, order: PlacementOrder) {
        _uiState.update { current ->
            current.copy(
                textRules = current.textRules.map { rule ->
                    if (rule.id == id) rule.copy(text = text, count = count, order = order)
                    else rule
                }
            )
        }
    }

    fun removeTextRule(id: String) {
        _uiState.update { current ->
            current.copy(textRules = current.textRules.filter { it.id != id })
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(validationError = null) }
    }

    fun dismissInfo() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun generateGrid() {
        val state = _uiState.value
        val rows = state.rowsInput.toIntOrNull() ?: 0
        val cols = state.colsInput.toIntOrNull() ?: 0
        val totalNumbers = state.totalNumbersInput.toIntOrNull() ?: 0

        val result = GridGenerator.generate(
            rows = rows,
            cols = cols,
            totalNumbers = totalNumbers,
            numOrder = state.numOrder,
            rules = state.textRules
        )

        when (result) {
            is GridGenerator.GenerationResult.Success -> {
                _uiState.update {
                    it.copy(
                        generatedGrid = result.grid,
                        validationError = null
                    )
                }
            }
            is GridGenerator.GenerationResult.Error -> {
                _uiState.update {
                    it.copy(validationError = result.message)
                }
            }
        }
    }

    fun exportAndShare(context: Context) {
        val grid = _uiState.value.generatedGrid
        if (grid == null || grid.cells.isEmpty()) {
            _uiState.update { it.copy(validationError = "پہلے Generate Grid پر کلک کریں!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val bitmap = withContext(Dispatchers.Default) {
                GridGenerator.createGridBitmap(grid)
            }

            val savedToGallery = withContext(Dispatchers.IO) {
                GridGenerator.saveToGallery(context, bitmap)
            }

            val uri = withContext(Dispatchers.IO) {
                GridGenerator.saveToCacheAndGetUri(context, bitmap)
            }

            _uiState.update {
                it.copy(
                    isExporting = false,
                    infoMessage = if (savedToGallery) "اعلیٰ کوالٹی (HD) تصویر محفوظ ہو گئی! (HD Image Saved)" else "HD تصویر تیار ہے!"
                )
            }

            if (uri != null) {
                GridGenerator.shareGridImage(context, uri)
            }
        }
    }

    fun exportAndSharePdf(context: Context) {
        val grid = _uiState.value.generatedGrid
        if (grid == null || grid.cells.isEmpty()) {
            _uiState.update { it.copy(validationError = "پہلے Generate Grid پر کلک کریں!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExportingPdf = true) }
            val uri = withContext(Dispatchers.IO) {
                GridGenerator.createGridPdf(context, grid)
            }

            _uiState.update {
                it.copy(
                    isExportingPdf = false,
                    infoMessage = if (uri != null) "سرکاری پی ڈی ایف شیٹ تیار ہے! (Official PDF Document Ready)" else "پی ڈی ایف بنانے میں خرابی پیش آگئی!"
                )
            }

            if (uri != null) {
                GridGenerator.shareGridPdf(context, uri)
            }
        }
    }
}
