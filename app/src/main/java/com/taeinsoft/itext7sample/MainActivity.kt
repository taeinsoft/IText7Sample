package com.taeinsoft.itext7sample

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private val FILE_TYPE = "FileType"
    private val FILE_TYPE_PDF = 101

    lateinit var btnWritePdf: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnWritePdf = findViewById(R.id.btn_write_pdf)
        btnWritePdf.setOnClickListener {
            writePdf()
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data.also { intent ->
                    intent?.data?.also { url ->
                        requestWritePdfData(url)
                    }
                }
            }
        }

    private fun requestWritePdfData(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            contentResolver.openFileDescriptor(uri, "w").use { descriptor ->
                if (descriptor != null) {
                    FileOutputStream(descriptor.fileDescriptor).use { stream ->
                        val writer = PdfWriter(stream)
                        val pdf = PdfDocument(writer)
                        val document = Document(pdf, PageSize.A4)
                        val content = "Hello World!!!"
                        val paragraph = Paragraph(content)

                        paragraph.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                        document.setMargins(50f, 50f, 50f, 50f)
                        document.add(paragraph)
                        document.close()
                    }
                }
            }
        }
    }

    private fun writePdf() {
        val fileName = "PdfSample"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(FILE_TYPE, FILE_TYPE_PDF)
            putExtra(Intent.EXTRA_TITLE, "$fileName.pdf")
        }
        launcher.launch(intent)
    }
}