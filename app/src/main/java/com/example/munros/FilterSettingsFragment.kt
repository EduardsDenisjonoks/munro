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

    private fun initDataBinding(binding: FilterSettingsFragmentBinding){
        binding.lifecycleOwner = viewLifecycleOwner
        binding.filterVm = viewModel
    }

    private fun initApplyButton(button: View){
        button.setOnClickListener {
            //todo apply changes
            findNavController().popBackStack()
        }
    }
}