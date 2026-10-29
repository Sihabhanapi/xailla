package com.xaillaclass.premium.ui
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xaillaclass.premium.R
import com.xaillaclass.premium.model.ServerConfig
class ConfigAdapter(private var items: List<ServerConfig>, private val onClick: (ServerConfig) -> Unit) : RecyclerView.Adapter<ConfigAdapter.VH>() {
    fun updateData(newItems: List<ServerConfig>) { items = newItems; notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_config, parent, false))
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvInfo.text = "${item.protocol.uppercase()} • ${item.address}:${item.port}"
        holder.itemView.setOnClickListener { onClick(item) }
    }
    override fun getItemCount() = items.size
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName); val tvInfo: TextView = v.findViewById(R.id.tvInfo)
    }
}
