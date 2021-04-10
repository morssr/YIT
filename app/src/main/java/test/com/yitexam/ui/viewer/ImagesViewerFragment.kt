package test.com.yitexam.ui.viewer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import test.com.yitexam.R
import test.com.yitexam.databinding.FragmentImagesViewerBinding

@AndroidEntryPoint
class ImagesViewerFragment : Fragment() {

    private val viewModel: ImagesViewerViewModel by viewModels()
    private val binding: FragmentImagesViewerBinding by viewBinding()
    private val args: ImagesViewerFragmentArgs by navArgs()

    private val adapter: PhotoViewerAdapter = PhotoViewerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_images_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        viewModel.getAllCashedImagesFlow().asLiveData().observe(viewLifecycleOwner, {
            adapter.setPhotos(it)
            binding.imagesPager.setCurrentItem(args.imagePosition, false)
        })
    }

    private fun initUi() {
        binding.imagesPager.adapter = adapter
    }

    companion object {
        private const val TAG = "ImagesViewerFragment"
    }
}