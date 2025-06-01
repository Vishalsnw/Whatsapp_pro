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
import androidx.core.content.edit
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FileListActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PICK_WHATSAPP_MEDIA = 101
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
        categoryTitle = findViewById(R.id.categoryTitle) // ← Fixed this line

        type = intent.getStringExtra("type") ?: return
        categoryTitle.text = "$type Files"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ SAF
            val savedUri = getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getString(PREF_WHATSAPP_MEDIA_URI, null)
            if (savedUri == null) {
                // Ask user to pick WhatsApp/Media folder
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
            // Legacy storage
            if (!FileScanner.hasStoragePermission(this)) {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            loadFilesLegacy()
        }

        deleteButton.setOnClickListener {
            val selected = adapter.getSelectedFiles()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pickedMediaUri != null) {
                deleteFilesSAF(selected)
            } else {
                for (fileModel in selected) {
                    fileModel.file?.delete()
                    fileList.remove(fileModel)
                }
                adapter.notifyDataSetChanged()
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

    // SAF deletion
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
                // Persist permission
                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                // Save to prefs
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
}
