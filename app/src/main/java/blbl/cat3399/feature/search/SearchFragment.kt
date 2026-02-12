package blbl.cat3399.feature.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import blbl.cat3399.R
import blbl.cat3399.core.log.AppLog
import blbl.cat3399.core.model.BangumiSeason
import blbl.cat3399.databinding.FragmentSearchBinding
import blbl.cat3399.feature.my.MyBangumiDetailFragment
import blbl.cat3399.ui.BackPressHandler
import blbl.cat3399.ui.RefreshKeyHandler

class SearchFragment : Fragment(), BackPressHandler, RefreshKeyHandler {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val state = SearchState()
    private var renderer: SearchRenderer? = null
    private var interactor: SearchInteractor? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val interactor = SearchInteractor(fragment = this, state = state)
        val renderer = SearchRenderer(fragment = this, binding = binding, state = state, interactor = interactor)
        interactor.renderer = renderer

        this.interactor = interactor
        this.renderer = renderer

        interactor.reloadHistory()
        renderer.setupInput()
        renderer.setupResults()
        renderer.applyUiScale()
        interactor.loadHotAndDefault()
        interactor.scheduleMiddleList(state.query)

        if (savedInstanceState == null) {
            renderer.showInput()
            renderer.focusFirstKey()
        }
    }

    override fun handleBackPressed(): Boolean {
        val b = _binding ?: return false
        val r = renderer ?: return false
        val resultsVisible = b.panelResults.visibility == View.VISIBLE
        AppLog.d("Back", "SearchFragment handleBackPressed resultsVisible=$resultsVisible")
        if (!resultsVisible) return false
        r.showInput()
        r.focusFirstKey()
        return true
    }

    override fun onResume() {
        super.onResume()
        renderer?.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            // Returning from Bangumi/Media detail uses add+hide, so SearchFragment won't get onResume().
            // Re-run the minimal "shown again" hooks to restore focus and other pending UI actions.
            renderer?.onShown()
        }
    }

    override fun handleRefreshKey(): Boolean {
        val b = _binding ?: return false
        if (!isResumed) return false
        if (b.panelResults.visibility != View.VISIBLE) return false
        if (b.swipeRefresh.isRefreshing) return true
        interactor?.resetAndLoad()
        return true
    }

    override fun onDestroyView() {
        interactor?.release()
        interactor = null
        renderer?.release()
        renderer = null
        state.lastAppliedUiScale = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = SearchFragment()
    }

    internal fun openBangumiDetail(season: BangumiSeason, isDrama: Boolean) {
        if (!isAdded || parentFragmentManager.isStateSaved) return
        // Use add+hide instead of replace so SearchFragment's view state (results panel, scroll, focus)
        // is preserved when returning from the detail page, matching the behavior of activity navigations.
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .hide(this)
            .add(
                R.id.main_container,
                MyBangumiDetailFragment.newInstance(
                    seasonId = season.seasonId,
                    isDrama = isDrama,
                    continueEpId = null,
                    continueEpIndex = null,
                ),
            )
            .addToBackStack(null)
            .commit()
    }
}
