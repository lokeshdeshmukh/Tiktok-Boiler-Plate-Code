

package com.loki.tiktok.adapter

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.loki.tiktok.R
import com.loki.tiktok.interfaces.OptiPositionListener

class OptiPositionAdapter(positionList: ArrayList<String>, val context: Context, optiPositionListener: OptiPositionListener) :
    RecyclerView.Adapter<OptiPositionAdapter.MyPostViewHolder>() {

    private var tagName: String = OptiPositionAdapter::class.java.simpleName
    private var myPositionList = positionList
    private var myPositionListener = optiPositionListener
    private var selectedPosition: Int = -1
    private var selectedPositionItem: String? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyPostViewHolder {
        return MyPostViewHolder(LayoutInflater.from(context).inflate(R.layout.playback_view, p0, false))
    }

    override fun getItemCount(): Int {
        return myPositionList.size
    }

    override fun onBindViewHolder(holder: MyPostViewHolder, position: Int) {

        holder.tvSpeed.text = myPositionList[position]

        if (selectedPosition == position) {
            holder.tvSpeed.setBackgroundColor(Color.WHITE)
            holder.tvSpeed.setTextColor(Color.BLACK)
        } else {
            holder.tvSpeed.setBackgroundColor(Color.BLACK)
            holder.tvSpeed.setTextColor(Color.WHITE)
        }

        holder.tvSpeed.setOnClickListener {
            //selected position will be saved here
            selectedPosition = position
            selectedPositionItem = myPositionList[holder.adapterPosition]
            notifyDataSetChanged()
        }
    }

    fun setPosition() {
        if(selectedPositionItem != null) {
            Log.v(tagName, "selectedPositionItem: $selectedPositionItem")
            myPositionListener.selectedPosition(selectedPositionItem!!)
        }
    }

    class MyPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSpeed: TextView = itemView.findViewById(R.id.tv_speed)
    }
}