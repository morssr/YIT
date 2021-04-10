package test.com.yitexam.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import test.com.yitexam.R
import test.com.yitexam.databinding.FragmentChooserBinding

@AndroidEntryPoint
class ChooserFragment : Fragment() {

    private val binding: FragmentChooserBinding by viewBinding()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chooser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            button.setOnClickListener { navigateToFixedImageHeightConfiguration() }
            button2.setOnClickListener { navigateToFlexibleImageHeightConfiguration() }
        }
    }

    private fun navigateToFixedImageHeightConfiguration() {
        findNavController().navigate(
            ChooserFragmentDirections.actionChooserFragmentToImagesFragment(
                true
            )
        )
    }

    private fun navigateToFlexibleImageHeightConfiguration() {
        findNavController().navigate(
            ChooserFragmentDirections.actionChooserFragmentToImagesFragment(
                false
            )
        )
    }

    companion object {
        private const val TAG = "ChooserFragment"
    }
}