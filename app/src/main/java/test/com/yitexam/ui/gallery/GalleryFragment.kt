package test.com.yitexam.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.*
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import test.com.yitexam.R
import test.com.yitexam.data.Image
import test.com.yitexam.data.LOAD_CASHED_DATA_FLAG
import test.com.yitexam.databinding.GalleryFragmentBinding
import test.com.yitexam.utilities.ConnectivityUtil
import test.com.yitexam.utilities.ShareUtils
import test.com.yitexam.utilities.extensions.hideKeyboard
import test.com.yitexam.utilities.extensions.px

@AndroidEntryPoint
class GalleryFragment : Fragment(), GalleryAdapter.ImagesAdapterListener {

    private val viewModel: GalleryViewModel by viewModels()
    private val binding: GalleryFragmentBinding by viewBinding()

    private val args: GalleryFragmentArgs by navArgs()

    private val adapter = GalleryAdapter(this)
    private var tracker: SelectionTracker<Image>? = null

    private var searchJob: Job? = null

    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.gallery_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        setupLoadStateAdapter()
        setupListLayoutManager()
        setupSelectionTracker(savedInstanceState)

        loadLastSearchCashedData()
        prepareSearch()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_images_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {
                setupSearchView(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupSearchView(item: MenuItem) {
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener, SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!ConnectivityUtil.hasNetworkAvailable(requireContext())) {
                    Log.w(TAG, "onQueryTextSubmit: No network connection. Search canceled.")
                    ConnectivityUtil.showInternetConnectionLost(requireContext())
                    return false
                }

                query?.let { search(query) }
                try {
                    requireActivity().hideKeyboard()
                    searchView.clearFocus()
                } catch (e: Exception) {
                    Log.e(TAG, "onQueryTextSubmit: ", e)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    override fun onImageClicked(image: Image, position: Int) {
        Log.d(TAG, "onImageClicked() called with: image = [$image], position = [$position]")
        val action = GalleryFragmentDirections.actionImagesFragmentToImagesViewerFragment(position)
        actionMode?.finish()
        findNavController().navigate(action)
    }

    private fun setupListLayoutManager() {
        val spanCount = resources.getInteger(R.integer.images_grid_span_count)
        val manager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        if (args.fixedHeight) adapter.spanSizeHeight =
            resources.getInteger(R.integer.fixed_gallery_span_height_dp).px

        binding.imagesGrid.run {
            layoutManager = manager
            itemAnimator = null
        }
    }

    private fun setupSelectionTracker(savedInstanceState: Bundle?) {
        tracker = SelectionTracker.Builder(
            "image-selection",
            binding.imagesGrid,
            GalleryKeyProvider(adapter),
            GalleryItemDetailsLookup(binding.imagesGrid),
            StorageStrategy.createParcelableStorage(Image::class.java)
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build().also {
            it.addObserver(object : SelectionTracker.SelectionObserver<Image>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    Log.d(TAG, "onSelectionChanged() called")
                    if (actionMode == null) startContextualToolbar()
                    else if (tracker?.selection?.size() == 0) actionMode!!.finish()
                }
            })
        }

        adapter.tracker = tracker

        if (savedInstanceState != null) tracker?.onRestoreInstanceState(savedInstanceState)
    }

    private fun setupLoadStateAdapter() {
        binding.imagesGrid.adapter = adapter.withLoadStateHeaderAndFooter(
            header = GalleryLoadStateAdapter { adapter.retry() },
            footer = GalleryLoadStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener { loadState ->
            // show empty lis
            val isListEmpty = loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0
            showEmptyList(isListEmpty)

            // Only show the list if refresh succeeds.
            binding.imagesGrid.isVisible = loadState.mediator?.refresh is LoadState.NotLoading
            // Show loading spinner during initial load or refresh.
            binding.progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
            // Show the retry state if initial load or refresh fails.
            binding.retryButton.isVisible = loadState.mediator?.refresh is LoadState.Error
            // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Toast.makeText(
                    requireContext(),
                    "${it.error}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun prepareSearch() {
        // Scroll to top when the list is refreshed from network.
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow
                .distinctUntilChangedBy { it.mediator }
                .filterNotNull()
                // Only react to cases when Remote REFRESH called on new search.
                .filter { it.mediator?.refresh is LoadState.Loading }
                .collect {
                    binding.imagesGrid.scrollToPosition(0)
                }
        }
    }

    private fun loadLastSearchCashedData() {
        Log.d(TAG, "loadLastSearchCashedData: called")
        search(LOAD_CASHED_DATA_FLAG)
    }

    private fun search(query: String) {
        Log.i(TAG, "search: start remote search with query string: $query")
        cancelSearchJob()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchImages(query).flowOn(IO).collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun cancelSearchJob() {
        searchJob?.cancel()
    }

    private fun setupToolbar() {
        Log.d(TAG, "setupToolbar: called")
        (requireActivity() as AppCompatActivity).run {
            setSupportActionBar(binding.toolbar.toolbar)
            supportActionBar?.let {
                it.title = getString(R.string.app_name)
                it.setHomeButtonEnabled(true)
                it.setDisplayShowTitleEnabled(true)
            }
        }
        setHasOptionsMenu(true)
    }

    private fun startContextualToolbar() {
        val activity = (requireActivity() as AppCompatActivity)
        val callback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                if (mode != null) actionMode = mode
                mode?.menuInflater?.inflate(R.menu.images_contextual_action_bar, menu)
                requireActivity().hideKeyboard()
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return when (item?.itemId) {
                    R.id.action_share -> {
                        val selectedImagesUrl = tracker?.selection?.map { it.webFormatUrl }
                        ShareUtils.shareMultipleImagesURLs(requireActivity(), selectedImagesUrl)
                        true
                    }
                    else -> false
                }
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                tracker?.clearSelection()
                actionMode = null
            }
        }
        activity.startSupportActionMode(callback)
    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            binding.emptyList.visibility = View.VISIBLE
            binding.imagesGrid.visibility = View.GONE
        } else {
            binding.emptyList.visibility = View.GONE
            binding.imagesGrid.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val TAG = "GalleryFragment"
    }
}