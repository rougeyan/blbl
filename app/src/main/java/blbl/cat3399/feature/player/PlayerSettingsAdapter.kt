package blbl.cat3399.feature.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import blbl.cat3399.databinding.ItemPlayerSettingBinding

class PlayerSettingsAdapter(
    private val onClick: (SettingItem) -> Unit,
) : ListAdapter<PlayerSettingsAdapter.SettingItem, PlayerSettingsAdapter.Vh>(DIFF) {
    data class SettingItem(
        val title: String,
        val subtitle: String,
    )

    init {
        setHasStableIds(true)
    }

    fun submit(list: List<SettingItem>) {
        submitList(list.toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemPlayerSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) = holder.bind(getItem(position), onClick)

    override fun getItemId(position: Int): Long {
        return getItem(position).title.hashCode().toLong()
    }

    fun indexOfTitle(title: String): Int {
        val index = currentList.indexOfFirst { it.title == title }
        return if (index >= 0) index else RecyclerView.NO_POSITION
    }

    private companion object {
        private val DIFF =
            object : DiffUtil.ItemCallback<SettingItem>() {
                override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
                    return oldItem.title == newItem.title
                }

                override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
                    return oldItem == newItem
                }
            }
    }

    class Vh(private val binding: ItemPlayerSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingItem, onClick: (SettingItem) -> Unit) {
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.subtitle
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
