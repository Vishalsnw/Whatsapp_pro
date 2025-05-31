package com.example.whatsappcleanerpro

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FileListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FileAdapter
    private lateinit var deleteButton: Button
    private lateinit var categoryTitle: TextView
    private lateinit var fileList: MutableList<FileModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        recyclerView = findViewById(R.id.fileRecyclerView)
        deleteButton = findViewById(R.id.deleteSelectedBtn)
        categoryTitle = findViewById(R.id.categoryTitle)

        val type = intent.getStringExtra("type") ?: return
        categoryTitle.text = "$type Files"

        // Assumes FileScanner.getFilesForCategory(type) returns List<FileModel>
        fileList = FileScanner.getFilesForCategory(type).toMutableList()
        // Pass onDeleteClicked lambda to FileAdapter if needed, else remove from constructor
        adapter = FileAdapter(fileList, onDeleteClicked = { fileModel ->
            fileModel.file.delete()
            fileList.remove(fileModel)
            adapter.notifyDataSetChanged()
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        deleteButton.setOnClickListener {
            val selected = adapter.getSelectedFiles()
            for (fileModel in selected) {
                fileModel.file.delete()
                fileList.remove(fileModel)
            }
            adapter.notifyDataSetChanged()
        }
    }
}
