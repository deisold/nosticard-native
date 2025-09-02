package solutions.appme.nosticard.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

interface ShareRepository {
    suspend fun sharePostcard(imagePath: String, message: String): Result<Unit>
    suspend fun sharePdf(pdfPath: String, message: String): Result<Unit>
}

class ShareRepositoryImpl(
    private val context: Context
) : ShareRepository {
    
    override suspend fun sharePostcard(imagePath: String, message: String): Result<Unit> {
        return try {
            val file = File(imagePath)
            if (!file.exists()) {
                return Result.failure(Exception("File not found: $imagePath"))
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(shareIntent, "Share Postcard")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun sharePdf(pdfPath: String, message: String): Result<Unit> {
        return try {
            val file = File(pdfPath)
            if (!file.exists()) {
                return Result.failure(Exception("PDF file not found: $pdfPath"))
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(shareIntent, "Share PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}