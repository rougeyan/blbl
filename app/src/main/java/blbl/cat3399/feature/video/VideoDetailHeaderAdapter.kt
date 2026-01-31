package blbl.cat3399.feature.video

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import blbl.cat3399.R
import blbl.cat3399.core.image.ImageLoader
import blbl.cat3399.core.image.ImageUrl
import blbl.cat3399.core.ui.UiScale
import blbl.cat3399.databinding.ItemVideoDetailHeaderBinding
import blbl.cat3399.feature.player.PlayerPlaylistItem
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

class VideoDetailHeaderAdapter(
    private val onPlayClick: () -> Unit,
    private val onUpClick: () -> Unit,
    private val onPartClick: (item: PlayerPlaylistItem, index: Int) -> Unit,
    private val onSeasonClick: (item: PlayerPlaylistItem, index: Int) -> Unit,
) : RecyclerView.Adapter<VideoDetailHeaderAdapter.Vh>() {
    private var holderRef: WeakReference<Vh>? = null

    private var title: String? = null
    private var desc: String? = null
    private var coverUrl: String? = null
    private var upName: String? = null
    private var upAvatar: String? = null
    private var seasonTitle: String? = null
    private var parts: List<PlayerPlaylistItem> = emptyList()
    private var seasonItems: List<PlayerPlaylistItem> = emptyList()
    private var seasonIndex: Int? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = 1L

    override fun getItemCount(): Int = 1

    fun requestFocusPlay(): Boolean = holderRef?.get()?.binding?.btnPlay?.requestFocus() == true

    fun invalidateSizing() {
        notifyItemChanged(0)
    }

    fun update(
        title: String?,
        desc: String?,
        coverUrl: String?,
        upName: String?,
        upAvatar: String?,
        seasonTitle: String?,
        parts: List<PlayerPlaylistItem>,
        seasonItems: List<PlayerPlaylistItem>,
        seasonIndex: Int?,
    ) {
        this.title = title
        this.desc = desc
        this.coverUrl = coverUrl
        this.upName = upName
        this.upAvatar = upAvatar
        this.seasonTitle = seasonTitle
        this.parts = parts
        this.seasonItems = seasonItems
        this.seasonIndex = seasonIndex
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        val binding = ItemVideoDetailHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Vh(binding, onPlayClick, onUpClick, onPartClick, onSeasonClick)
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.bind(
            title = title,
            desc = desc,
            coverUrl = coverUrl,
            upName = upName,
            upAvatar = upAvatar,
            seasonTitle = seasonTitle,
            parts = parts,
            seasonItems = seasonItems,
            seasonIndex = seasonIndex,
        )
    }

    override fun onViewAttachedToWindow(holder: Vh) {
        super.onViewAttachedToWindow(holder)
        holderRef = WeakReference(holder)
    }

    override fun onViewDetachedFromWindow(holder: Vh) {
        val current = holderRef?.get()
        if (current === holder) holderRef = null
        super.onViewDetachedFromWindow(holder)
    }

    class Vh(
        val binding: ItemVideoDetailHeaderBinding,
        private val onPlayClick: () -> Unit,
        private val onUpClick: () -> Unit,
        private val onPartClick: (item: PlayerPlaylistItem, index: Int) -> Unit,
        private val onSeasonClick: (item: PlayerPlaylistItem, index: Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val partsAdapter = VideoDetailPlaylistAdapter { item, index -> onPartClick(item, index) }
        private val seasonAdapter = VideoDetailPlaylistAdapter { item, index -> onSeasonClick(item, index) }
        private var lastUiScale: Float? = null
        private var lastSeasonAutoScrollKey: String? = null

        init {
            binding.btnPlay.setOnClickListener { onPlayClick() }
            binding.cardUp.setOnClickListener { onUpClick() }

            binding.recyclerParts.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerParts.adapter = partsAdapter

            binding.recyclerSeason.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerSeason.adapter = seasonAdapter
        }

        fun bind(
            title: String?,
            desc: String?,
            coverUrl: String?,
            upName: String?,
            upAvatar: String?,
            seasonTitle: String?,
            parts: List<PlayerPlaylistItem>,
            seasonItems: List<PlayerPlaylistItem>,
            seasonIndex: Int?,
        ) {
            val uiScale = UiScale.factor(binding.root.context)
            if (lastUiScale != uiScale) {
                applySizing(uiScale)
                lastUiScale = uiScale
            }

            binding.tvTitle.text = title?.trim().takeIf { !it.isNullOrBlank() } ?: "-"

            val safeCover = coverUrl?.trim().takeIf { !it.isNullOrBlank() }
            if (safeCover != null) {
                ImageLoader.loadInto(binding.ivCover, ImageUrl.cover(safeCover))
            }

            val safeUpName = upName?.trim().takeIf { !it.isNullOrBlank() }
            binding.cardUp.isVisible = safeUpName != null
            if (safeUpName != null) {
                binding.tvUpName.text = safeUpName
                ImageLoader.loadInto(binding.ivUpAvatar, ImageUrl.avatar(upAvatar))
            }

            val safeDesc = desc?.trim().takeIf { !it.isNullOrBlank() }
            binding.tvDesc.text = safeDesc ?: "暂无简介"

            val showParts = parts.size > 1
            binding.tvPartsHeader.isVisible = showParts
            binding.recyclerParts.isVisible = showParts
            if (showParts) {
                binding.tvPartsHeader.text = "分P（${parts.size}）"
                partsAdapter.submit(parts)
            } else {
                partsAdapter.submit(emptyList())
            }

            val showSeason = seasonItems.size > 1
            binding.tvSeasonHeader.isVisible = showSeason
            binding.recyclerSeason.isVisible = showSeason
            if (showSeason) {
                val safeTitle = seasonTitle?.trim().takeIf { !it.isNullOrBlank() }
                binding.tvSeasonHeader.text = safeTitle?.let { "合集：$it" } ?: "合集（${seasonItems.size}）"
                seasonAdapter.submit(seasonItems)
                maybeAutoScrollSeason(seasonItems, seasonIndex)
            } else {
                seasonAdapter.submit(emptyList())
                lastSeasonAutoScrollKey = null
            }
        }

        private fun maybeAutoScrollSeason(seasonItems: List<PlayerPlaylistItem>, seasonIndex: Int?) {
            val idx = seasonIndex?.takeIf { it in seasonItems.indices } ?: return
            val targetBvid = seasonItems[idx].bvid.trim()
            if (targetBvid.isBlank()) return

            val firstBvid = seasonItems.firstOrNull()?.bvid?.trim().orEmpty()
            val lastBvid = seasonItems.lastOrNull()?.bvid?.trim().orEmpty()
            val autoScrollKey = "$targetBvid|$idx|${seasonItems.size}|$firstBvid|$lastBvid"
            if (autoScrollKey == lastSeasonAutoScrollKey) return
            lastSeasonAutoScrollKey = autoScrollKey

            binding.recyclerSeason.post {
                val lm = binding.recyclerSeason.layoutManager as? LinearLayoutManager ?: return@post
                lm.scrollToPositionWithOffset(idx, binding.recyclerSeason.paddingLeft)
            }
        }

        private fun applySizing(uiScale: Float) {
            fun px(id: Int): Int = binding.root.resources.getDimensionPixelSize(id)
            fun pxF(id: Int): Float = binding.root.resources.getDimension(id)

            fun scaledPx(id: Int): Int = (px(id) * uiScale).roundToInt().coerceAtLeast(0)
            fun scaledPxF(id: Int): Float = pxF(id) * uiScale

            run {
                val ps = scaledPx(R.dimen.video_detail_header_padding_start_tv)
                val pt = scaledPx(R.dimen.video_detail_header_padding_top_tv)
                val pe = scaledPx(R.dimen.video_detail_header_padding_end_tv)
                val pb = scaledPx(R.dimen.video_detail_header_padding_bottom_tv)
                if (
                    binding.root.paddingStart != ps ||
                    binding.root.paddingTop != pt ||
                    binding.root.paddingEnd != pe ||
                    binding.root.paddingBottom != pb
                ) {
                    binding.root.setPaddingRelative(ps, pt, pe, pb)
                }
            }

            run {
                val w = scaledPx(R.dimen.video_detail_header_cover_width_tv).coerceAtLeast(1)
                val lp = binding.ivCover.layoutParams
                if (lp.width != w) {
                    lp.width = w
                    binding.ivCover.layoutParams = lp
                }
            }

            run {
                val ms = scaledPx(R.dimen.video_detail_header_title_margin_start_tv)
                val me = scaledPx(R.dimen.video_detail_header_title_margin_end_tv)
                (binding.tvTitle.layoutParams as? MarginLayoutParams)?.let { lp ->
                    if (lp.marginStart != ms || lp.marginEnd != me) {
                        lp.marginStart = ms
                        lp.marginEnd = me
                        binding.tvTitle.layoutParams = lp
                    }
                }
                binding.tvTitle.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    scaledPxF(R.dimen.video_detail_header_title_text_size_tv),
                )
            }

            run {
                val mt = scaledPx(R.dimen.video_detail_header_up_card_margin_top_tv)
                (binding.cardUp.layoutParams as? MarginLayoutParams)?.let { lp ->
                    if (lp.topMargin != mt) {
                        lp.topMargin = mt
                        binding.cardUp.layoutParams = lp
                    }
                }

                val radius = scaledPxF(R.dimen.video_detail_header_up_card_corner_radius_tv)
                if (binding.cardUp.radius != radius) binding.cardUp.radius = radius

                val strokeWidth = scaledPx(R.dimen.video_detail_header_up_card_stroke_width_tv)
                if (binding.cardUp.strokeWidth != strokeWidth) binding.cardUp.strokeWidth = strokeWidth

                val padH = scaledPx(R.dimen.video_detail_header_up_card_padding_h_tv)
                val padV = scaledPx(R.dimen.video_detail_header_up_card_padding_v_tv)
                val child = binding.cardUp.getChildAt(0)
                if (child != null) {
                    if (
                        child.paddingLeft != padH ||
                        child.paddingTop != padV ||
                        child.paddingRight != padH ||
                        child.paddingBottom != padV
                    ) {
                        child.setPadding(padH, padV, padH, padV)
                    }
                }

                val avatarSize = scaledPx(R.dimen.video_detail_header_up_avatar_size_tv).coerceAtLeast(1)
                val avatarLp = binding.ivUpAvatar.layoutParams
                if (avatarLp.width != avatarSize || avatarLp.height != avatarSize) {
                    avatarLp.width = avatarSize
                    avatarLp.height = avatarSize
                    binding.ivUpAvatar.layoutParams = avatarLp
                }

                val nameMs = scaledPx(R.dimen.video_detail_header_up_name_margin_start_tv)
                (binding.tvUpName.layoutParams as? MarginLayoutParams)?.let { lp ->
                    if (lp.marginStart != nameMs) {
                        lp.marginStart = nameMs
                        binding.tvUpName.layoutParams = lp
                    }
                }

                binding.tvUpName.maxWidth = scaledPx(R.dimen.video_detail_header_up_name_max_width_tv).coerceAtLeast(0)
                binding.tvUpName.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    scaledPxF(R.dimen.video_detail_header_up_name_text_size_tv),
                )
            }

            run {
                val mt = scaledPx(R.dimen.video_detail_header_desc_margin_top_tv)
                val me = scaledPx(R.dimen.video_detail_header_desc_margin_end_tv)
                (binding.tvDesc.layoutParams as? MarginLayoutParams)?.let { lp ->
                    if (lp.topMargin != mt || lp.marginEnd != me) {
                        lp.topMargin = mt
                        lp.marginEnd = me
                        binding.tvDesc.layoutParams = lp
                    }
                }
                binding.tvDesc.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    scaledPxF(R.dimen.video_detail_header_desc_text_size_tv),
                )
            }

            run {
                val mt = scaledPx(R.dimen.video_detail_header_play_margin_top_tv)
                (binding.btnPlay.layoutParams as? MarginLayoutParams)?.let { lp ->
                    if (lp.topMargin != mt) {
                        lp.topMargin = mt
                        binding.btnPlay.layoutParams = lp
                    }
                }

                binding.btnPlay.cornerRadius = scaledPx(R.dimen.video_detail_header_play_corner_radius_tv)
                binding.btnPlay.strokeWidth = scaledPx(R.dimen.video_detail_header_play_stroke_width_tv)
                binding.btnPlay.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    scaledPxF(R.dimen.video_detail_header_play_text_size_tv),
                )
            }

            run {
                val mt = scaledPx(R.dimen.video_detail_header_parts_margin_top_tv)
                (binding.tvPartsHeader.layoutParams as? MarginLayoutParams)?.let { lp ->
                    if (lp.topMargin != mt) {
                        lp.topMargin = mt
                        binding.tvPartsHeader.layoutParams = lp
                    }
                }
                val textSize = scaledPxF(R.dimen.video_detail_header_section_text_size_tv)
                binding.tvPartsHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }

            run {
                val pv = scaledPx(R.dimen.video_detail_header_playlist_padding_v_tv)
                if (binding.recyclerParts.paddingTop != pv || binding.recyclerParts.paddingBottom != pv) {
                    binding.recyclerParts.setPadding(
                        binding.recyclerParts.paddingLeft,
                        pv,
                        binding.recyclerParts.paddingRight,
                        pv,
                    )
                }
            }

            run {
                val mt = scaledPx(R.dimen.video_detail_header_season_margin_top_tv)
                (binding.tvSeasonHeader.layoutParams as? MarginLayoutParams)?.let { lp ->
                    if (lp.topMargin != mt) {
                        lp.topMargin = mt
                        binding.tvSeasonHeader.layoutParams = lp
                    }
                }
                val textSize = scaledPxF(R.dimen.video_detail_header_section_text_size_tv)
                binding.tvSeasonHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }

            run {
                val pv = scaledPx(R.dimen.video_detail_header_playlist_padding_v_tv)
                if (binding.recyclerSeason.paddingTop != pv || binding.recyclerSeason.paddingBottom != pv) {
                    binding.recyclerSeason.setPadding(
                        binding.recyclerSeason.paddingLeft,
                        pv,
                        binding.recyclerSeason.paddingRight,
                        pv,
                    )
                }
            }

            run {
                val mt = scaledPx(R.dimen.video_detail_header_recommend_margin_top_tv)
                (binding.tvRecommendHeader.layoutParams as? MarginLayoutParams)?.let { lp ->
                    if (lp.topMargin != mt) {
                        lp.topMargin = mt
                        binding.tvRecommendHeader.layoutParams = lp
                    }
                }
                val textSize = scaledPxF(R.dimen.video_detail_header_section_text_size_tv)
                binding.tvRecommendHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            }
        }
    }
}
