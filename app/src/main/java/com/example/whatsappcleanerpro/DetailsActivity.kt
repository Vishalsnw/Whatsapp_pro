package com.example.whatsappcleanerpro

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappcleanerpro.databinding.ActivityDetailsBinding
import java.io.File

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var filesList: MutableList<File>
    private lateinit var adapter: FilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("category") ?: "Files"
        binding.detailsTitle.text = "$categoryName Files"

        val categoryPath = intent.getStringExtra("categoryPath")
        if (categoryPath == null) {
            Toast.makeText(this, "Invalid category path", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val categoryDir = File(categoryPath)
        if (!categoryDir.exists() || !categoryDir.isDirectory) {
            Toast.makeText(this, "No files found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        filesList = categoryDir.listFiles()?.toMutableList() ?: mutableListOf()
        adapter = FilesAdapter(filesList) { file ->
            handleDeleteFile(file)
        }

        binding.detailsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.detailsRecyclerView.adapter = adapter
    }

    // Only one deleteFile method, no signature conflicts
    private fun handleDeleteFile(file: File) {
        if (file.delete()) {
            Toast.makeText(this, "Deleted: ${file.name}", Toast.LENGTH_SHORT).show()
            filesList.remove(file)
            adapter.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "Failed to delete: ${file.name}", Toast.LENGTH_SHORT).show()
        }
    }
}
