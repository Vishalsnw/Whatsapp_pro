package com.example.whatsappcleanerpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(
    private val files: List<FileModel>,
    private val onDeleteClicked: (File) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    inner class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvFileName)
        val tvSize: TextView = view.findViewById(R.id.tvFileSize)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.tvName.text = file.name
        holder.tvSize.text = "${file.size / 1024} KB"
        holder.btnDelete.setOnClickListener {
            onDeleteClicked(file.file)
        }
    }

    override fun getItemCount() = files.size
}
