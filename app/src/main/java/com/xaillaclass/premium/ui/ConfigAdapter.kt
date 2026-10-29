package com.xaillaclass.premium.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xaillaclass.premium.R
import com.xaillaclass.premium.model.ServerConfig

class ConfigAdapter(
    private var items: List<ServerConfig>,
    private val onSelect: (ServerConfig) -> Unit,
    private val onEdit: (ServerConfig) -> Unit,
    private val onCopy: (ServerConfig) -> Unit,
    private val onDelete: (ServerConfig) -> Unit
) : RecyclerView.Adapter<ConfigAdapter.VH>() {

    fun updateData(newItems: List<ServerConfig>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_config, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvInfo.text = "${item.protocol.uppercase()} • ${item.address}:${item.port}"
        
        // Klik utama = pilih config
        holder.itemView.setOnClickListener { onSelect(item) }

        // Klik titik 3 = menu popup
        holder.btnOptions.setOnClickListener { btn ->
            val popup = PopupMenu(btn.context, btn)
            popup.menuInflater.inflate(R.menu.menu_item_options, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> onEdit(item)
                    R.id.action_copy -> onCopy(item)
                    R.id.action_delete -> onDelete(item)
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvInfo: TextView = v.findViewById(R.id.tvInfo)
        val btnOptions: ImageButton = v.findViewById(R.id.btnOptions)
    }
}
