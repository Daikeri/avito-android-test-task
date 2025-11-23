package com.example.listofbooks

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bookslist.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBooksScreen(
    viewModel: MyBooksViewModel = hiltViewModel(),
    onBookClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MyBooksEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои книги") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Поиск книг…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Контент
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is MyBooksUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is MyBooksUiState.Empty -> {
                        EmptyStateMessage("У вас пока нет книг", Modifier.align(Alignment.Center))
                    }
                    is MyBooksUiState.Error -> {
                        ErrorStateMessage(
                            message = "Ошибка загрузки данных",
                            onRetry = viewModel::loadBooks,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is MyBooksUiState.Success -> {
                        if (state.filteredBooks.isEmpty()) {
                            EmptyStateMessage("Ничего не найдено", Modifier.align(Alignment.Center))
                        } else {
                            BooksList(
                                books = state.filteredBooks,
                                onBookClick = onBookClick,
                                onActionClick = viewModel::onBookAction
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BooksList(
    books: List<Book>,
    onBookClick: (String) -> Unit,
    onActionClick: (Book) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(books, key = { it.id }) { book ->
            BookItem(book, onBookClick, onActionClick)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun BookItem(
    book: Book,
    onClick: (String) -> Unit,
    onAction: (Book) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (book.isDownloaded && book.localPath != null) {
                    onClick(book.localPath!!)
                }
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp, 80.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = when(book.extension.lowercase()) {
                        "pdf" -> Icons.Default.PictureAsPdf
                        "txt" -> Icons.Default.Description
                        "epub" -> Icons.Default.Book
                        else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = book.extension.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // --- ИНФОРМАЦИЯ О КНИГЕ ---
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = { onAction(book) }) {
            if (book.isDownloaded) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Загрузить",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun EmptyStateMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
fun ErrorStateMessage(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}