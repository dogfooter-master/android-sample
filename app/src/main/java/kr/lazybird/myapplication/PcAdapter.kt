package kr.lazybird.myapplication

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.pc_list_item.view.*

class PcAdapter(val items : ArrayList<Agent>, val context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {
    interface ItemClick
    {
        fun onClick(view: View, position: Int)
    }
    var itemClick: ItemClick? = null

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        if(itemClick != null)
        {
            p0?.itemView?.setOnClickListener { v ->
                itemClick?.onClick(v, p1)
            }
        }
        p0?.tvPcType?.text = items.get(p1).name
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.pc_list_item, p0, false))
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }
}

class ViewHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val tvPcType = view.tv_pc_type
}