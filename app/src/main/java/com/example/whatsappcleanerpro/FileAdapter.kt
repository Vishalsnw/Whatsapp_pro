package com.example.whatsappcleanerpro

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappcleanerpro.databinding.ItemFileBinding
import java.io.File

class FilesAdapter(
    private val files: List<File>,
    private val onDeleteClicked: (File) -> Unit
) : RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.binding.fileName.text = file.name
        holder.binding.fileSize.text = "${file.length() / 1024} KB"

        holder.binding.deleteBtn.setOnClickListener {
            onDeleteClicked(file)
        }
    }

    override fun getItemCount(): Int = files.size
}
