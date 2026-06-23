package androidx.activity.compose

import androidx.compose.runtime.Composable
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import javax.swing.JFileChooser
import java.io.File
import android.net.Uri
import javax.swing.SwingUtilities

@Composable
fun <I, O> rememberLauncherForActivityResult(
    contract: ActivityResultContract<I, O>,
    onResult: (O) -> Unit
): ManagedActivityResultLauncher<I, O> {
    return ManagedActivityResultLauncher(contract, onResult)
}

class ManagedActivityResultLauncher<I, O>(
    private val contract: ActivityResultContract<I, O>,
    private val onResult: (O) -> Unit
) {
    fun launch(input: I) {
        SwingUtilities.invokeLater {
            val fileChooser = JFileChooser().apply {
                // Set default directory to user's home folder
                currentDirectory = File(System.getProperty("user.home"))
            }
            when (contract) {
                is ActivityResultContracts.OpenDocument -> {
                    val result = fileChooser.showOpenDialog(null)
                    if (result == JFileChooser.APPROVE_OPTION) {
                        val file = fileChooser.selectedFile
                        val uri = android.net.Uri.parse("file://" + file.absolutePath)
                        @Suppress("UNCHECKED_CAST")
                        onResult(uri as O)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        onResult(null as O)
                    }
                }
                is ActivityResultContracts.OpenDocumentTree -> {
                    fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    val result = fileChooser.showOpenDialog(null)
                    if (result == JFileChooser.APPROVE_OPTION) {
                        val file = fileChooser.selectedFile
                        val uri = android.net.Uri.parse("file://" + file.absolutePath)
                        @Suppress("UNCHECKED_CAST")
                        onResult(uri as O)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        onResult(null as O)
                    }
                }
                is ActivityResultContracts.CreateDocument -> {
                    if (input is String) {
                        fileChooser.selectedFile = File(fileChooser.currentDirectory, input)
                    }
                    val result = fileChooser.showSaveDialog(null)
                    if (result == JFileChooser.APPROVE_OPTION) {
                        val file = fileChooser.selectedFile
                        val uri = android.net.Uri.parse("file://" + file.absolutePath)
                        @Suppress("UNCHECKED_CAST")
                        onResult(uri as O)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        onResult(null as O)
                    }
                }
                else -> {
                    @Suppress("UNCHECKED_CAST")
                    onResult(true as O)
                }
            }
        }
    }
}
