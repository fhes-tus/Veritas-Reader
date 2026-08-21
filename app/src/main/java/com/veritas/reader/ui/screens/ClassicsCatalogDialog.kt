package com.veritas.reader.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.veritas.reader.SavedDocument

data class ClassicBookEntry(
    val id: String,
    val title: String,
    val author: String,
    val genre: String,
    val description: String,
    val downloadUrl: String,
    val estimatedMinutes: Int,
    val coverGradient: List<Color>,
    val accentColor: Color
)

val CURATED_CLASSICS = listOf(
    ClassicBookEntry(
        id = "classic_meditations",
        title = "Meditations",
        author = "Marcus Aurelius",
        genre = "Philosophy",
        description = "Timeless personal reflections on self-discipline, resilience, virtue, and tranquility from the Roman emperor.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/2680/pg2680.txt",
        estimatedMinutes = 180,
        coverGradient = listOf(Color(0xFF2C1654), Color(0xFF4A2574)),
        accentColor = Color(0xFFFFD54F)
    ),
    ClassicBookEntry(
        id = "classic_art_of_war",
        title = "The Art of War",
        author = "Sun Tzu",
        genre = "Strategy",
        description = "The ancient masterpiece on military strategy, conflict resolution, mental agility, and overcoming obstacles.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/132/pg132.txt",
        estimatedMinutes = 90,
        coverGradient = listOf(Color(0xFF5A1818), Color(0xFF8B2525)),
        accentColor = Color(0xFFFFCC80)
    ),
    ClassicBookEntry(
        id = "classic_as_a_man_thinketh",
        title = "As a Man Thinketh",
        author = "James Allen",
        genre = "Mindset",
        description = "The foundational guide showing how our thoughts shape character, health, circumstances, and personal achievement.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/4507/pg4507.txt",
        estimatedMinutes = 45,
        coverGradient = listOf(Color(0xFF1B3B2B), Color(0xFF2E6347)),
        accentColor = Color(0xFFA5D6A7)
    ),
    ClassicBookEntry(
        id = "classic_sherlock_holmes",
        title = "The Adventures of Sherlock Holmes",
        author = "Arthur Conan Doyle",
        genre = "Mystery",
        description = "Twelve iconic cases solved by the brilliant consulting detective Sherlock Holmes and Dr. John Watson.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/1661/pg1661.txt",
        estimatedMinutes = 320,
        coverGradient = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
        accentColor = Color(0xFF80DEEA)
    ),
    ClassicBookEntry(
        id = "classic_frankenstein",
        title = "Frankenstein",
        author = "Mary Shelley",
        genre = "Sci-Fi & Gothic",
        description = "The pioneering tale of Victor Frankenstein and the monstrous creature brought to life through forbidden science.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/84/pg84.txt",
        estimatedMinutes = 260,
        coverGradient = listOf(Color(0xFF132F2B), Color(0xFF1E463F)),
        accentColor = Color(0xFF80CBC4)
    ),
    ClassicBookEntry(
        id = "classic_pride_and_prejudice",
        title = "Pride and Prejudice",
        author = "Jane Austen",
        genre = "Literature",
        description = "The witty, beloved romantic drama following Elizabeth Bennet and Mr. Darcy in 19th-century England.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/1342/pg1342.txt",
        estimatedMinutes = 420,
        coverGradient = listOf(Color(0xFF4A1525), Color(0xFF6B1D36)),
        accentColor = Color(0xFFFFAB91)
    ),
    ClassicBookEntry(
        id = "classic_the_prince",
        title = "The Prince",
        author = "Niccolò Machiavelli",
        genre = "Strategy",
        description = "The influential political treatise exploring power dynamics, leadership, pragmatism, and statecraft.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/1232/pg1232.txt",
        estimatedMinutes = 140,
        coverGradient = listOf(Color(0xFF3E1F17), Color(0xFF5D2E22)),
        accentColor = Color(0xFFFFCC80)
    )
)

/**
 * Full-screen Classics & Books Catalog browser with rich styled book covers and OceanOfPDF integration.
 */
@Composable
fun ClassicsCatalogDialog(
    existingDocuments: List<SavedDocument>,
    onDownloadBook: (ClassicBookEntry) -> Unit,
    onOpenOceanOfPdf: (searchQuery: String) -> Unit = {},
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }
    var downloadingId by remember { mutableStateOf<String?>(null) }

    val genres = remember {
        listOf("All", "Philosophy", "Strategy", "Mindset", "Mystery", "Literature", "Sci-Fi & Gothic")
    }

    val filteredList = remember(searchQuery, selectedGenre) {
        CURATED_CLASSICS.filter { book ->
            val matchesGenre = selectedGenre == "All" || book.genre.contains(selectedGenre, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.author.contains(searchQuery, ignoreCase = true) ||
                book.genre.contains(searchQuery, ignoreCase = true)
            matchesGenre && matchesSearch
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Header (Seamless Background)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                            }
                            Column {
                                Text(
                                    "Classic Books Catalog",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Public domain masterpieces",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Sleek Subtle Ocean of PDF Action
                        Surface(
                            onClick = { onOpenOceanOfPdf(searchQuery) },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Language,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    "Ocean of PDF",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Search & Genre Filter Row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search catalog or type any book title…") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(genres) { genre ->
                            FilterChip(
                                selected = selectedGenre == genre,
                                onClick = { selectedGenre = genre },
                                label = { Text(genre) }
                            )
                        }
                    }
                }

                // Catalog List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // If no local match, prominent OceanOfPDF search card
                    if (filteredList.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Filled.Language,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        "Search \"$searchQuery\" on Ocean of PDF",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Explore millions of free books, novels, and bestsellers with 1-tap download into Veritas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = { onOpenOceanOfPdf(searchQuery) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Search Ocean of PDF")
                                    }
                                }
                            }
                        }
                    }

                    items(filteredList, key = { it.id }) { book ->
                        val isInstalled = existingDocuments.any {
                            it.title.contains(book.title, ignoreCase = true) ||
                            it.originalFileName.contains(book.id, ignoreCase = true)
                        }
                        val isDownloading = downloadingId == book.id

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Styled Book Cover Artwork
                                Box(
                                    modifier = Modifier
                                        .width(76.dp)
                                        .height(108.dp)
                                        .shadow(6.dp, RoundedCornerShape(6.dp))
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.verticalGradient(book.coverGradient))
                                        .border(1.dp, book.accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            book.author.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = book.accentColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 7.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            book.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            "VERITAS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 6.sp,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                // Book Metadata & Action
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                book.title,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                "${book.author} • ${book.genre}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        if (isInstalled) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = CircleShape,
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Text(
                                                        "Added",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    downloadingId = book.id
                                                    onDownloadBook(book)
                                                },
                                                enabled = !isDownloading,
                                                shape = CircleShape,
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                if (isDownloading) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(11.dp),
                                                        strokeWidth = 2.dp,
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                } else {
                                                    Icon(
                                                        Icons.Filled.Download,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Get", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        book.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        "⏱️ ~${book.estimatedMinutes} min read",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    // OceanOfPDF footer discovery card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clickable { onOpenOceanOfPdf("") },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Language,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Explore Ocean of PDF",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        "Download EPUB & PDF books directly into Veritas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
