package com.igorunderplayer.untexteditor

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


const val PERMISSION_REQUEST_CODE = 31

class FileEdit : AppCompatActivity() {
    private lateinit var saveButton: Button
    private lateinit var  textView: EditText

    private var fileStream: InputStream? = null

    public var textFile: TextFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_edit)

        textView = findViewById(R.id.editTextView)
        saveButton = findViewById(R.id.editSaveButton)

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

        if (!checkPermission()) {
            requestPermission()
        }

        val action = intent.action

        if (Intent.ACTION_VIEW == action) {
            val uri = intent.data

            Log.d("ta aqui o uri", uri.toString())

            openFile(uri!!)
        } else {
            Log.d("Uai", "intent was something else: $action")
        }
    }

    private fun checkPermission(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_TEXT_FILE && resultCode == RESULT_OK) {
            Log.d("Arquivo aberto", data?.data.toString())


            data?.data?.also { documentUri ->
                contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                openFile(documentUri)
            }
        } else {
            if (requestCode == 2296) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data =
                    Uri.parse(String.format("package:%s", applicationContext.packageName))
                startActivityForResult(intent, 2296)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE);
        }
    }
}