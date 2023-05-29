package com.subhamgupta.httpserver.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhamgupta.httpserver.R
import com.subhamgupta.httpserver.databinding.UserItemBinding
import com.subhamgupta.httpserver.domain.model.User

class UserAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val userList = mutableListOf<User>()

    inner class MyHolder(parent: ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)

    ){
        private val binding = UserItemBinding.bind(itemView)

        fun onBind(user: User){
            binding.username.text = user.username
            binding.lastLogin.text = user.timestamp.epochSeconds.toString()
            binding.status.text = user.status
        }

    }
    @SuppressLint("NotifyDataSetChanged")
    fun setItems(userList: List<User>?) {
        this.userList.clear()
        this.userList.addAll(userList?: emptyList())
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyHolder).onBind(userList[position])
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}