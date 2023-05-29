package com.subhamgupta.httpserver.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.httpserver.R
import com.subhamgupta.httpserver.databinding.FileItemBinding
import com.subhamgupta.httpserver.domain.model.ReceivedFile

class ReceivedFileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = mutableListOf<ReceivedFile>()

    inner class MyHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)

    ) {
        private val binding = FileItemBinding.bind(itemView)

        fun onBind(receivedFile: ReceivedFile) {
            binding.fileName.text = receivedFile.fileName
            binding.open.setOnClickListener {

            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun  setList(receivedFileList: List<ReceivedFile>){
        list.clear()
        list.addAll(receivedFileList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyHolder(parent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyHolder).onBind(list[position])
    }
}