package androidx.activity.compose

import androidx.compose.runtime.Composable
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import java.awt.FileDialog
import java.awt.Frame
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
            when (contract) {
                is ActivityResultContracts.OpenDocument -> {
                    val dialog = FileDialog(null as Frame?, "Select Document", FileDialog.LOAD).apply {
                        directory = System.getProperty("user.home")
                        isVisible = true
                    }
                    val file = dialog.file
                    val dir = dialog.directory
                    if (file != null && dir != null) {
                        val selectedFile = File(dir, file)
                        val uri = android.net.Uri.parse("file://" + selectedFile.absolutePath)
                        @Suppress("UNCHECKED_CAST")
                        onResult(uri as O)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        onResult(null as O)
                    }
                }
                is ActivityResultContracts.OpenDocumentTree -> {
                    // JFileChooser remains the most direct way to pick directories in AWT/Swing without external native libraries
                    val fileChooser = javax.swing.JFileChooser().apply {
                        currentDirectory = File(System.getProperty("user.home"))
                        fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
                    }
                    val result = fileChooser.showOpenDialog(null)
                    if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
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
                    val dialog = FileDialog(null as Frame?, "Save Document", FileDialog.SAVE).apply {
                        directory = System.getProperty("user.home")
                        if (input is String) {
                            file = input
                        }
                        isVisible = true
                    }
                    val file = dialog.file
                    val dir = dialog.directory
                    if (file != null && dir != null) {
                        val selectedFile = File(dir, file)
                        val uri = android.net.Uri.parse("file://" + selectedFile.absolutePath)
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
