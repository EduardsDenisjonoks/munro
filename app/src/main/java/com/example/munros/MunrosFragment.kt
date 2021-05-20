package com.example.munros

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.munros.adapters.MunroAdapter
import com.example.munros.databinding.MunrosFragmentBinding

class MunrosFragment : Fragment() {

    private val viewModel: MunrosViewModel by viewModels()
    private val munroAdapter by lazy { MunroAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<MunrosFragmentBinding>(
            inflater,
            R.layout.munros_fragment,
            container,
            false
        )

        initDataBinding(binding)
        initListView(binding.listView)
        initLoadDataButton(binding.btnLoadData)
        initFilterDataButton(binding.fabFilterData)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    private fun initDataBinding(binding: MunrosFragmentBinding){
        binding.lifecycleOwner = viewLifecycleOwner
        binding.munrosViewModel = viewModel
    }

    private fun initListView(listView: RecyclerView){
        listView.adapter = munroAdapter
        listView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initLoadDataButton(button: View){
        button.setOnClickListener {
            loadData()
        }
    }

    private fun initFilterDataButton(button: View){
        button.setOnClickListener {
            findNavController().navigate(R.id.action_munros_to_filter_settings)
        }
    }


    private fun initObservers() {
        viewModel.getMunrosLiveData().observe(viewLifecycleOwner) { munros ->
            munroAdapter.setMunros(munros)
        }
        viewModel.getErrorLiveData().observe(viewLifecycleOwner) { errorMessage ->
            showErrorDialog(errorMessage)
        }
    }

    private fun loadData() {
        viewModel.loadCsvFromInputStream(requireContext().assets.open("munrotab_v6.2.csv"))
    }

    private fun showErrorDialog(errorMessage: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_error)
            .setMessage(errorMessage)
            .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }
}