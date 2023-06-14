package com.subhamgupta.httpserver.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.httpserver.R
import com.subhamgupta.httpserver.databinding.AccessItemBinding
import com.subhamgupta.httpserver.domain.model.FolderObj

class AccessItemAdapter(
    val selectedFolder: MutableSet<FolderObj>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val folderList = mutableListOf<FolderObj>()

    inner class MyHolder(parent: ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.access_item, parent, false)

    ){
        private val binding = AccessItemBinding.bind(itemView)

        fun onBind(folderObj: FolderObj){
            binding.folderItem.text = folderObj.filename
            binding.folderItem.isChecked = selectedFolder.contains(folderObj)
            binding.folderItem.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    selectedFolder.add(folderObj)
                }else{
                    selectedFolder.remove(folderObj)
                }
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFolderList(folderList: List<FolderObj>){
        this.folderList.clear()
        this.folderList.addAll(folderList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccessItemAdapter.MyHolder {
        return MyHolder(parent)
    }

    override fun getItemCount(): Int {
        return folderList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyHolder).onBind(folderList[position])
    }
}