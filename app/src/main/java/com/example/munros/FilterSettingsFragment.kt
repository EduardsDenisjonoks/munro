package com.example.munros

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.munros.databinding.FilterSettingsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterSettingsFragment : Fragment() {

    private val viewModel: FilterSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FilterSettingsFragmentBinding>(
            inflater,
            R.layout.filter_settings_fragment,
            container,
            false
        )

        initDataBinding(binding)
        initApplyButton(binding.btnApply)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    private fun initDataBinding(binding: FilterSettingsFragmentBinding) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.filterVm = viewModel
    }

    private fun initApplyButton(button: View) {
        button.setOnClickListener {
            viewModel.applyChanges {
               findNavController().popBackStack()
            }
        }
    }

    private fun initObservers() {
        viewModel.getCachedCategoryLiveData().observe(viewLifecycleOwner) { category ->
            viewModel.setCategory(category)
        }
        viewModel.getCachedMinHeightLiveData().observe(viewLifecycleOwner) { height ->
            viewModel.setMinHeight(height)
        }
        viewModel.getCachedMaxHeightLiveData().observe(viewLifecycleOwner) { height ->
            viewModel.setMaxHeight(height)
        }
        viewModel.getCachedItemLimitLiveData().observe(viewLifecycleOwner) { limit ->
            viewModel.setItemLimit(limit)
        }
        viewModel.getCachedMaxHeightLiveData().observe(viewLifecycleOwner) { height ->
            viewModel.setMaxHeight(height)
        }
        viewModel.getCachedHeightSortOptionLiveData().observe(viewLifecycleOwner) { option ->
            viewModel.setHeightSortOption(option)
        }
        viewModel.getCachedNameSortOptionLiveData().observe(viewLifecycleOwner) { option ->
            viewModel.setNameSortOption(option)
        }
    }
}