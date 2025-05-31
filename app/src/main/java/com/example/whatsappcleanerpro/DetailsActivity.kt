package com.example.whatsappcleanerpro

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsappcleanerpro.databinding.ActivityDetailsBinding
import java.io.File

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var filesList: MutableList<FileModel>
    private lateinit var adapter: FileAdapter

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
        if (!categoryDir.existscategoryDir.isDirectory) {
            Toast.makeText(this, "No files found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        filesList = categoryDir.listFiles()
            ?.map { file ->
                FileModel(
                    file = file,
                    name = file.name,
                    size = file.length(),
                    path = file.absolutePath
                )
            }?.toMutableList() ?: mutableListOf()

        adapter = FileAdapter(filesList) { fileModel ->
            // Inline delete logic since handleDeleteFile was unresolved
            val index = filesList.indexOf(fileModel)
            if (index != -1 && fileModel.file.delete()) {
                Toast.makeText(this, "Deleted: ${fileModel.name}", Toast.LENGTH_SHORT).show()
                filesList.removeAt(index)
                adapter.notifyItemRemoved(index)
            } else {
                Toast.makeText(this, "Failed to delete: ${fileModel.name}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.detailsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.detailsRecyclerView.adapter = adapter
    }
}
