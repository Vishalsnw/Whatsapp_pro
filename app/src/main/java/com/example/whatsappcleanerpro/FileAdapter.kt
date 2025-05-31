package com.example.whatsappcleanerpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(
    private val files: MutableList<FileModel>,
    private val onDeleteClicked: (FileModel) -> Unit
) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    // For tracking selected files
    private val selectedFiles = mutableSetOf<FileModel>()

    inner class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvFileName)
        val tvSize: TextView = view.findViewById(R.id.tvFileSize)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val chkSelect: CheckBox = view.findViewById(R.id.chkSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val fileModel = files[position]
        holder.tvName.text = fileModel.name
        holder.tvSize.text = "${fileModel.size / 1024} KB"

        // Checkbox handling
        holder.chkSelect.setOnCheckedChangeListener(null)
        holder.chkSelect.isChecked = selectedFiles.contains(fileModel)
        holder.chkSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFiles.add(fileModel) else selectedFiles.remove(fileModel)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClicked(fileModel)
        }
    }

    override fun getItemCount() = files.size

    // Add this method to match FileListActivity usage
    fun getSelectedFiles(): List<FileModel> = selectedFiles.toList()
}
