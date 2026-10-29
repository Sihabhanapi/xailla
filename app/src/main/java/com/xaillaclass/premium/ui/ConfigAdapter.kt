package com.xaillaclass.premium.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.xaillaclass.premium.R
import com.xaillaclass.premium.model.ServerConfig

class ConfigAdapter(
    private var items: List<ServerConfig>,
    private var selectedId: String?,
    private val onSelect: (ServerConfig) -> Unit,
    private val onEditBasic: (ServerConfig) -> Unit,
    private val onEditFull: (ServerConfig) -> Unit,
    private val onCopy: (ServerConfig) -> Unit,
    private val onSave: (ServerConfig) -> Unit,
    private val onDuplicate: (ServerConfig) -> Unit,
    private val onDelete: (ServerConfig) -> Unit
) : RecyclerView.Adapter<ConfigAdapter.VH>() {

    fun updateData(newItems: List<ServerConfig>, newSelectedId: String?) {
        items = newItems
        selectedId = newSelectedId
        notifyDataSetChanged()
    }

    fun updateSelection(newSelectedId: String?) {
        selectedId = newSelectedId
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

        // Highlight hijau jika terpilih
        if (item.id == selectedId) {
            holder.card.setCardBackgroundColor(Color.parseColor("#D1FAE5")) // Hijau muda
            holder.tvName.setTextColor(Color.parseColor("#065F46")) // Hijau tua
            holder.tvInfo.setTextColor(Color.parseColor("#047857"))
        } else {
            holder.card.setCardBackgroundColor(
                com.google.android.material.color.MaterialColors.getColor(
                    holder.card, com.google.android.material.R.attr.colorSurface
                )
            )
            holder.tvName.setTextColor(
                com.google.android.material.color.MaterialColors.getColor(
                    holder.card, com.google.android.material.R.attr.colorOnSurface
                )
            )
            holder.tvInfo.setTextColor(
                com.google.android.material.color.MaterialColors.getColor(
                    holder.card, com.google.android.material.R.attr.colorOnSurfaceVariant
                )
            )
        }

        holder.itemView.setOnClickListener { onSelect(item) }

        holder.btnOptions.setOnClickListener { btn ->
            val popup = PopupMenu(btn.context, btn)
            popup.menuInflater.inflate(R.menu.menu_item_options, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit_basic -> onEditBasic(item)
                    R.id.action_edit_full -> onEditFull(item)
                    R.id.action_copy -> onCopy(item)
                    R.id.action_save -> onSave(item)
                    R.id.action_duplicate -> onDuplicate(item)
                    R.id.action_delete -> onDelete(item)
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val card: MaterialCardView = v.findViewById(R.id.cardConfig)
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvInfo: TextView = v.findViewById(R.id.tvInfo)
        val btnOptions: ImageButton = v.findViewById(R.id.btnOptions)
    }
}
