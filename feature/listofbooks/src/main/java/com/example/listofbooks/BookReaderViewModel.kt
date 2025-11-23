package com.example.listofbooks

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.File
import javax.inject.Inject

data class ReaderUiState(
    val isLoading: Boolean = true,
    val paragraphs: List<String> = emptyList(),

    val fontSizeSp: Float = 18f,
    val lineHeightMultiplier: Float = 1.2f,   // <-- НОВОЕ

    val initialIndex: Int = 0,
    val initialOffset: Int = 0,

    val errorMessage: String? = null
)

@HiltViewModel
class BookReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val localPath: String = checkNotNull(savedStateHandle["localPath"])
    private val prefs = context.getSharedPreferences("reader_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState

    private var currentIndex = 0
    private var currentOffset = 0

    init {
        loadState()
        loadBook()
    }

    private fun loadState() {
        _uiState.update {
            it.copy(
                fontSizeSp = prefs.getFloat("fontSizeSp", 18f),
                lineHeightMultiplier = prefs.getFloat("lineHeight", 1.2f),
                initialIndex = prefs.getInt("progress_${localPath}_i", 0),
                initialOffset = prefs.getInt("progress_${localPath}_o", 0)
            )
        }
    }

    private fun saveProgress() {
        prefs.edit()
            .putInt("progress_${localPath}_i", currentIndex)
            .putInt("progress_${localPath}_o", currentOffset)
            .apply()
    }

    fun onScrollChanged(index: Int, offset: Int) {
        currentIndex = index
        currentOffset = offset
        saveProgress()
    }

    fun onFontSizeChanged(v: Float) {
        prefs.edit().putFloat("fontSizeSp", v).apply()
        _uiState.update { it.copy(fontSizeSp = v) }
    }

    fun onLineHeightChanged(v: Float) {
        prefs.edit().putFloat("lineHeight", v).apply()
        _uiState.update { it.copy(lineHeightMultiplier = v) }
    }

    private fun loadBook() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val res = withContext(Dispatchers.IO) {
                try {
                    val file = File(localPath)
                    if (!file.exists())
                        return@withContext Result.failure<String>(Exception("Файл не найден"))

                    val ext = file.extension.lowercase()
                    Log.e("FORMAT FILE", ext)

                    val text = when (ext) {
                        "txt" -> file.readText()
                        "epub" -> parseEpub(file)
                        "pdf" -> parsePdf(file)
                        else -> throw IllegalArgumentException("Формат .$ext не поддерживается")
                    }

                    Result.success(text)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

            res.onSuccess { text ->
                val parts = text
                    .replace("\r\n", "\n")
                    .split("\n\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        paragraphs = parts,
                        errorMessage = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    private fun parseEpub(file: File): String {
        val reader = EpubReader()
        val book = file.inputStream().use { reader.readEpub(it) }

        val html = book.contents.joinToString("\n") {
            String(it.data, Charsets.UTF_8)
        }

        return Jsoup.parse(html).text()
    }

    private fun parsePdf(file: File): String {
        PDFBoxResourceLoader.init(context)

        PDDocument.load(file).use { doc ->
            val stripper = PDFTextStripper()
            return stripper.getText(doc)
        }
    }
}
