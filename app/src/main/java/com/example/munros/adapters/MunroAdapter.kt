package com.example.munros.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.munrolibrary.Munro
import com.example.munros.R
import com.example.munros.databinding.ListItemMunroBinding

class MunroAdapter : RecyclerView.Adapter<MunroAdapter.MunroViewHolder>(){

    private val munroList = ArrayList<Munro>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MunroViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MunroViewHolder(
            DataBindingUtil.inflate(
                inflater,
                R.layout.list_item_munro,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MunroViewHolder, position: Int) {
        holder.bindView(munroList[position])
    }

    override fun getItemCount(): Int = munroList.size

    fun setMunros(newMunros: List<Munro>){
        val diffResult = DiffUtil.calculateDiff(
            DiffCallback(munroList, newMunros)
        )
        munroList.clear()
        munroList.addAll(newMunros)
        diffResult.dispatchUpdatesTo(this)
    }


    inner class MunroViewHolder(private val binding: ListItemMunroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(munro: Munro) {
            binding.name = munro.name
            binding.height = munro.heightInMeters.toString()
            binding.category = munro.category
            binding.gridRef = munro.gridRef
        }
    }

    class DiffCallback(private var oldItems: List<Munro>, private var newItems: List<Munro>) :
        DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldItems[oldItemPosition] == newItems[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldItems[oldItemPosition] == newItems[newItemPosition]
                    && oldItems[oldItemPosition].name == newItems[newItemPosition].name
                    && oldItems[oldItemPosition].heightInMeters == newItems[newItemPosition].heightInMeters
                    && oldItems[oldItemPosition].category == newItems[newItemPosition].category
                    && oldItems[oldItemPosition].gridRef == newItems[newItemPosition].gridRef
    }
}