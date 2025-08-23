package com.mobi.nubitalcatel.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobi.nubitalcatel.data.viewModel.WidgetViewModel
import com.mobi.nubitalcatel.databinding.FragmentNubitViewBinding
import com.mobi.nubitalcatel.ui.adapters.WidgetAdapter

class NubitFragment : Fragment() {

    private var _binding: FragmentNubitViewBinding? = null
    private val binding get() = _binding!!

    private val widgetViewModel: WidgetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNubitViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = WidgetAdapter(emptyList())
        binding.rvWidgets.adapter = adapter
        binding.rvWidgets.layoutManager = LinearLayoutManager(requireContext())

        widgetViewModel.widgets.observe(viewLifecycleOwner) { widgets ->
            if (!widgets.isNullOrEmpty()) {
                val sorted = widgets.sortedBy { it.sort_order }
                binding.rvWidgets.adapter = WidgetAdapter(sorted)
            }
        }

        widgetViewModel.loadWidgets(order = "latest")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
