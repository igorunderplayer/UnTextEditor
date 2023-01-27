package com.igorunderplayer.untexteditor

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


const val PICK_TEXT_FILE = 3

class MainActivity : AppCompatActivity() {

    private lateinit var testButton: Button
    private lateinit var saveButton: Button
    private lateinit var  textView: EditText

    private var fileStream: InputStream? = null

    public var textFile: TextFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        textView = findViewById(R.id.editTextView)
        testButton = findViewById(R.id.testButton)
        saveButton = findViewById(R.id.saveButton)

        testButton.setOnClickListener {
            Log.d("Main:openFile", "Open file button clicked")

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/*"
            }

            startActivityForResult(intent, PICK_TEXT_FILE)
        }

        saveButton.setOnClickListener {
            textFile?.updateText(textView.text.toString())
            try {
                textFile?.let { file ->
                    contentResolver.openFileDescriptor(file.fileUri, "wt")?.use {
                        FileOutputStream(it.fileDescriptor).use {
                            it.write(textFile?.inputStream?.readBytes())
                            Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (_: IOException) {}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_TEXT_FILE && resultCode == RESULT_OK) {
            Log.d("Main:file_picked:", data?.data.toString())

            data?.data?.also { documentUri ->
                contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                openFile(documentUri)
            }
        }
    }

    private fun openFile(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri) ?: return

        textFile = TextFile(uri, inputStream)
        textView.setText(textFile?.readAllText())
    }

    private fun closeFile() {
        fileStream?.close()
    }
}