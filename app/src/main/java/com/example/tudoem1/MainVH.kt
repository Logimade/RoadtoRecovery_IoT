package com.example.tudoem1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.mroczis.netmonster.core.model.cell.ICell
import com.example.tudoem1.databinding.ViewCellBinding

class MainVH(
    private val binding: ViewCellBinding
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun create(parent: ViewGroup): MainVH =
            MainVH(
                ViewCellBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
    }

    fun bind(cell: ICell) = binding.root.bind(cell)

}