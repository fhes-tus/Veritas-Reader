package com.veritas.reader.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

data class FreeBookSite(
    val name: String,
    val description: String,
    val icon: String,
    val url: String
)

val FREE_BOOK_SITES = listOf(
    FreeBookSite("Project Gutenberg", "70,000+ public domain classics & epubs", "🏛️", "https://www.gutenberg.org"),
    FreeBookSite("Standard Ebooks", "Beautifully typeset, modern public domain releases", "✨", "https://standardebooks.org"),
    FreeBookSite("Open Library", "Over 3 million books to borrow & read online", "📖", "https://openlibrary.org"),
    FreeBookSite("ManyBooks", "50,000+ free digital titles across all genres", "📚", "https://manybooks.net"),
    FreeBookSite("Ocean of PDF", "Comprehensive search for books & manuscripts", "🌊", "https://oceanofpdf.com")
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
    ),
    ClassicBookEntry(
        id = "classic_dorian_gray",
        title = "The Picture of Dorian Gray",
        author = "Oscar Wilde",
        genre = "Literature",
        description = "A philosophical gothic novel of aestheticism, moral corruption, and a portrait that bears the sins of youth.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/174/pg174.txt",
        estimatedMinutes = 240,
        coverGradient = listOf(Color(0xFF261C3B), Color(0xFF3D2C5E)),
        accentColor = Color(0xFFE1BEE7)
    ),
    ClassicBookEntry(
        id = "classic_dracula",
        title = "Dracula",
        author = "Bram Stoker",
        genre = "Sci-Fi & Gothic",
        description = "The definitive vampire classic detailing Count Dracula's attempt to move from Transylvania to England.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/345/pg345.txt",
        estimatedMinutes = 450,
        coverGradient = listOf(Color(0xFF380808), Color(0xFF5E1010)),
        accentColor = Color(0xFFEF9A9A)
    ),
    ClassicBookEntry(
        id = "classic_alice_wonderland",
        title = "Alice's Adventures in Wonderland",
        author = "Lewis Carroll",
        genre = "Fantasy",
        description = "The whimsical surrealist journey of a young girl falling through a rabbit hole into a fantasy realm.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/11/pg11.txt",
        estimatedMinutes = 120,
        coverGradient = listOf(Color(0xFF1A365D), Color(0xFF2B6CB0)),
        accentColor = Color(0xFF90CDF4)
    ),
    ClassicBookEntry(
        id = "classic_time_machine",
        title = "The Time Machine",
        author = "H.G. Wells",
        genre = "Sci-Fi & Gothic",
        description = "The seminal science fiction work introducing time travel, journeying to the year 802,701 AD.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/35/pg35.txt",
        estimatedMinutes = 110,
        coverGradient = listOf(Color(0xFF1C3A27), Color(0xFF2D5A3E)),
        accentColor = Color(0xFF9AE6B4)
    ),
    ClassicBookEntry(
        id = "classic_great_expectations",
        title = "Great Expectations",
        author = "Charles Dickens",
        genre = "Literature",
        description = "The bildungsroman tracing the growth, trials, and moral development of the orphan Pip.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/1400/pg1400.txt",
        estimatedMinutes = 520,
        coverGradient = listOf(Color(0xFF3B2F1B), Color(0xFF5E492B)),
        accentColor = Color(0xFFFBD38D)
    ),
    ClassicBookEntry(
        id = "classic_moby_dick",
        title = "Moby Dick",
        author = "Herman Melville",
        genre = "Literature",
        description = "The legendary epic voyage of the Pequod and Captain Ahab's monomaniacal quest for the white whale.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/2701/pg2701.txt",
        estimatedMinutes = 680,
        coverGradient = listOf(Color(0xFF102A43), Color(0xFF243B53)),
        accentColor = Color(0xFF9FB3C8)
    ),
    ClassicBookEntry(
        id = "classic_the_republic",
        title = "The Republic",
        author = "Plato",
        genre = "Philosophy",
        description = "Socratic dialogue on justice, the order and character of the just city-state, and the virtuous person.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/1497/pg1497.txt",
        estimatedMinutes = 480,
        coverGradient = listOf(Color(0xFF44271A), Color(0xFF653A27)),
        accentColor = Color(0xFFFFD1B2)
    ),
    ClassicBookEntry(
        id = "classic_walden",
        title = "Walden",
        author = "Henry David Thoreau",
        genre = "Philosophy",
        description = "A reflection upon simple living in natural surroundings and personal declaration of independence.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/205/pg205.txt",
        estimatedMinutes = 280,
        coverGradient = listOf(Color(0xFF20382B), Color(0xFF335C45)),
        accentColor = Color(0xFFC6F6D5)
    ),
    ClassicBookEntry(
        id = "classic_the_prophet",
        title = "The Prophet",
        author = "Kahlil Gibran",
        genre = "Philosophy",
        description = "Twenty-six poetic essays on love, marriage, children, giving, work, joy, sorrow, and freedom.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/58585/pg58585.txt",
        estimatedMinutes = 65,
        coverGradient = listOf(Color(0xFF4A3410), Color(0xFF745219)),
        accentColor = Color(0xFFFEEBC8)
    ),
    ClassicBookEntry(
        id = "classic_metamorphosis",
        title = "The Metamorphosis",
        author = "Franz Kafka",
        genre = "Literature",
        description = "The unsettling masterpiece where traveling salesman Gregor Samsa awakens transformed into a giant insect.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/5200/pg5200.txt",
        estimatedMinutes = 80,
        coverGradient = listOf(Color(0xFF2D3748), Color(0xFF4A5568)),
        accentColor = Color(0xFFCBD5E0)
    ),
    ClassicBookEntry(
        id = "classic_monte_cristo",
        title = "The Count of Monte Cristo",
        author = "Alexandre Dumas",
        genre = "Literature",
        description = "The ultimate tale of betrayal, hidden treasure, enduring patience, retribution, and redemption.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/1184/pg1184.txt",
        estimatedMinutes = 850,
        coverGradient = listOf(Color(0xFF321E1E), Color(0xFF532E2E)),
        accentColor = Color(0xFFFEB2B2)
    ),
    ClassicBookEntry(
        id = "classic_nietzsche_beyond",
        title = "Beyond Good and Evil",
        author = "Friedrich Nietzsche",
        genre = "Philosophy",
        description = "A scathing critique of past philosophers and an examination of morality, truth, and the will to power.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/4363/pg4363.txt",
        estimatedMinutes = 260,
        coverGradient = listOf(Color(0xFF322659), Color(0xFF44337A)),
        accentColor = Color(0xFFD6BCFA)
    ),
    ClassicBookEntry(
        id = "classic_tale_two_cities",
        title = "A Tale of Two Cities",
        author = "Charles Dickens",
        genre = "Literature",
        description = "Set in London and Paris during the French Revolution, depicting sacrifice, love, and redemption.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/98/pg98.txt",
        estimatedMinutes = 390,
        coverGradient = listOf(Color(0xFF521B28), Color(0xFF702437)),
        accentColor = Color(0xFFFED7E2)
    ),
    ClassicBookEntry(
        id = "classic_yellow_wallpaper",
        title = "The Yellow Wallpaper",
        author = "Charlotte Perkins Gilman",
        genre = "Sci-Fi & Gothic",
        description = "A powerful psychological narrative chronicling a woman's gradual descent into madness within a confined room.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/1952/pg1952.txt",
        estimatedMinutes = 40,
        coverGradient = listOf(Color(0xFF5F4E12), Color(0xFF8D731B)),
        accentColor = Color(0xFFFEFCBF)
    ),
    ClassicBookEntry(
        id = "classic_jekyll_hyde",
        title = "Dr Jekyll and Mr Hyde",
        author = "Robert Louis Stevenson",
        genre = "Mystery",
        description = "The gothic investigation into the duality of human nature, civil morality, and hidden darker instincts.",
        downloadUrl = "https://www.gutenberg.org/cache/epub/43/pg43.txt",
        estimatedMinutes = 95,
        coverGradient = listOf(Color(0xFF1A202C), Color(0xFF2D3748)),
        accentColor = Color(0xFFA0AEC0)
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
    onOpenBookBrowser: (url: String, name: String, searchQuery: String) -> Unit = { _, _, q -> onOpenOceanOfPdf(q) },
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }
    var downloadingId by remember { mutableStateOf<String?>(null) }

    val genres = remember {
        listOf("All", "Philosophy", "Strategy", "Mindset", "Mystery", "Literature", "Sci-Fi & Gothic", "Fantasy")
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

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Free Online Book Archives",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "In-App Browser",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(FREE_BOOK_SITES) { site ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.clickable {
                                    onOpenBookBrowser(site.url, site.name, searchQuery)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(site.icon, fontSize = 14.sp)
                                    Column {
                                        Text(
                                            site.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            site.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
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
                                        "Search \"$searchQuery\" on Free Book Sites",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Explore millions of free books with sandboxed 1-tap download into Veritas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        FREE_BOOK_SITES.forEach { site ->
                                            Button(
                                                onClick = { onOpenBookBrowser(site.url, site.name, searchQuery) },
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = if (site.name == "Ocean of PDF") ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors()
                                            ) {
                                                Text("${site.icon} Search ${site.name}")
                                            }
                                        }
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
                                // Vintage Styled Book Cover Artwork
                                Box(
                                    modifier = Modifier
                                        .width(78.dp)
                                        .height(114.dp)
                                        .shadow(8.dp, RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp, topStart = 2.dp, bottomStart = 2.dp))
                                        .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp, topStart = 2.dp, bottomStart = 2.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    book.coverGradient.first().copy(alpha = 0.95f),
                                                    book.coverGradient.first(),
                                                    book.coverGradient.last()
                                                )
                                            )
                                        )
                                ) {
                                    // Vintage Book Spine Crease Effect
                                    Box(
                                        modifier = Modifier
                                            .width(5.dp)
                                            .fillMaxHeight()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color.Black.copy(alpha = 0.5f),
                                                        Color.White.copy(alpha = 0.15f),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )

                                    // Embossed gold/accent border inside cover
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(start = 9.dp, end = 5.dp, top = 5.dp, bottom = 5.dp)
                                            .border(1.dp, book.accentColor.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                            .padding(5.dp)
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
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 7.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    "✦",
                                                    color = book.accentColor.copy(alpha = 0.75f),
                                                    fontSize = 6.sp
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    book.title,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Serif,
                                                    fontSize = 9.5.sp,
                                                    lineHeight = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                "VERITAS CLASSIC",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = book.accentColor.copy(alpha = 0.85f),
                                                fontSize = 5.5.sp,
                                                letterSpacing = 0.8.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
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

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
