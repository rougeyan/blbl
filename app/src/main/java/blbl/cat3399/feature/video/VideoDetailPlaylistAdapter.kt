package blbl.cat3399.feature.video

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView
import blbl.cat3399.R
import blbl.cat3399.databinding.ItemVideoDetailPlaylistBinding
import blbl.cat3399.feature.player.PlayerPlaylistItem
import blbl.cat3399.core.ui.UiScale
import kotlin.math.roundToInt

class VideoDetailPlaylistAdapter(
    private val onClick: (item: PlayerPlaylistItem, position: Int) -> Unit,
) : RecyclerView.Adapter<VideoDetailPlaylistAdapter.Vh>() {
    private val items = ArrayList<PlayerPlaylistItem>()

    init {
        setHasStableIds(true)
    }

    fun submit(list: List<PlayerPlaylistItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        val item = items[position]
        val key =
            buildString {
                append(item.bvid)
                append('|')
                append(item.cid ?: -1L)
                append('|')
                append(item.aid ?: -1L)
                append('|')
                append(item.title.orEmpty())
            }
        return key.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemVideoDetailPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size

    class Vh(private val binding: ItemVideoDetailPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        private var lastUiScale: Float? = null

        fun bind(item: PlayerPlaylistItem, onClick: (item: PlayerPlaylistItem, position: Int) -> Unit) {
            val uiScale = UiScale.factor(binding.root.context)
            if (lastUiScale != uiScale) {
                applySizing(uiScale)
                lastUiScale = uiScale
            }

            binding.btn.text = item.title?.trim().takeIf { !it.isNullOrBlank() } ?: "视频"
            binding.btn.setOnClickListener {
                val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return@setOnClickListener
                onClick(item, pos)
            }
        }

        private fun applySizing(uiScale: Float) {
            fun px(id: Int): Int = binding.root.resources.getDimensionPixelSize(id)
            fun pxF(id: Int): Float = binding.root.resources.getDimension(id)

            fun scaledPx(id: Int): Int = (px(id) * uiScale).roundToInt().coerceAtLeast(0)
            fun scaledPxF(id: Int): Float = pxF(id) * uiScale

            val h = scaledPx(R.dimen.video_detail_playlist_item_height_tv).coerceAtLeast(1)
            val lp = binding.root.layoutParams
            if (lp.height != h) {
                lp.height = h
                binding.root.layoutParams = lp
            }

            val margin = scaledPx(R.dimen.video_detail_playlist_item_margin_tv)
            (binding.root.layoutParams as? MarginLayoutParams)?.let { mlp ->
                if (mlp.leftMargin != margin || mlp.topMargin != margin || mlp.rightMargin != margin || mlp.bottomMargin != margin) {
                    mlp.setMargins(margin, margin, margin, margin)
                    binding.root.layoutParams = mlp
                }
            }

            val minWidth = scaledPx(R.dimen.video_detail_playlist_item_min_width_tv).coerceAtLeast(0)
            if (binding.root.minWidth != minWidth) binding.root.minWidth = minWidth

            binding.root.cornerRadius = scaledPx(R.dimen.video_detail_playlist_item_corner_radius_tv)
            binding.root.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                scaledPxF(R.dimen.video_detail_playlist_item_text_size_tv),
            )
        }
    }
}
