package com.example.whatsappcleanerpro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.content.pm.PackageManager

class FileListActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PICK_WHATSAPP_MEDIA = 101
        private const val REQUEST_CODE_PERMISSION_STORAGE = 200
        private const val PREF_WHATSAPP_MEDIA_URI = "pref_whatsapp_media_uri"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileAdapter
    private lateinit var deleteButton: Button
    private lateinit var categoryTitle: TextView
    private lateinit var fileList: MutableList<FileModel>
    private var pickedMediaUri: Uri? = null
    private var type: String = "media"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        recyclerView = findViewById(R.id.fileRecyclerView)
        deleteButton = findViewById(R.id.deleteSelectedBtn)
        categoryTitle = findViewById(R.id.categoryTitle)

        type = intent.getStringExtra("type") ?: "media"
        categoryTitle.text = "$type Files"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val savedUri = getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getString(PREF_WHATSAPP_MEDIA_URI, null)
            if (savedUri == null) {
                Toast.makeText(this, "Please select WhatsApp/Media folder", Toast.LENGTH_LONG).show()
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                )
                startActivityForResult(intent, REQUEST_CODE_PICK_WHATSAPP_MEDIA)
                return
            } else {
                pickedMediaUri = Uri.parse(savedUri)
                loadFilesSAF()
            }
        } else {
            // Android 10 and below
            if (!hasStoragePermission()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSION_STORAGE
                )
                return
            }
            loadFilesLegacy()
        }

        deleteButton.setOnClickListener {
            val selected = adapter.getSelectedFiles()
            if (selected.isEmpty()) {
                Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pickedMediaUri != null) {
                deleteFilesSAF(selected)
            } else {
                for (fileModel in selected) {
                    fileModel.file?.delete()
                    fileList.remove(fileModel)
                }
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Deleted ${selected.size} file(s)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFilesLegacy() {
        fileList = FileScanner.getFilesForCategory(this, type).toMutableList()
        setupAdapter()
    }

    private fun loadFilesSAF() {
        if (pickedMediaUri == null) return
        fileList = FileScanner.getFilesForCategorySAF(this, pickedMediaUri!!, type).toMutableList()
        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = FileAdapter(
            files = fileList,
            onDeleteClicked = { fileModel ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pickedMediaUri != null) {
                    deleteFilesSAF(listOf(fileModel))
                } else {
                    fileModel.file?.delete()
                    fileList.remove(fileModel)
                    adapter.notifyDataSetChanged()
                }
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun deleteFilesSAF(files: List<FileModel>) {
        var deletedCount = 0
        for (fileModel in files) {
            val docFile = DocumentFile.fromSingleUri(this, Uri.parse(fileModel.path))
            if (docFile?.delete() == true) {
                fileList.remove(fileModel)
                deletedCount++
            }
        }
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Deleted $deletedCount file(s)", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_WHATSAPP_MEDIA && resultCode == Activity.RESULT_OK) {
            val treeUri = data?.data
            if (treeUri != null) {
                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                getSharedPreferences("prefs", Context.MODE_PRIVATE).edit {
                    putString(PREF_WHATSAPP_MEDIA_URI, treeUri.toString())
                }
                pickedMediaUri = treeUri
                loadFilesSAF()
            } else {
                Toast.makeText(this, "No folder selected.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Legacy storage permission check
    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Handle permission result for legacy Android
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFilesLegacy()
            } else {
                Toast.makeText(this, "Storage permission is required to view files", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
