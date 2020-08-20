package com.broprojects.studentcalendar.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.broprojects.studentcalendar.R
import com.broprojects.studentcalendar.databinding.FragmentTestBinding

class TestFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentTestBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_test, container, false
        )

        val viewModelFactory = TestViewModelFactory(requireActivity())
        val viewModel = ViewModelProvider(this, viewModelFactory)[TestViewModel::class.java]
        binding.viewModel = viewModel

        // Set app color theme on views
        viewModel.colorStateList.observe(viewLifecycleOwner, Observer {
            binding.saveButton.backgroundTintList = it
            binding.courseTextLayout.setBoxStrokeColorStateList(it)
            binding.typeTextLayout.setBoxStrokeColorStateList(it)
            binding.titleTextLayout.setBoxStrokeColorStateList(it)
            binding.whenTextLayout.setBoxStrokeColorStateList(it)
            binding.infoTextLayout.setBoxStrokeColorStateList(it)
        })

        viewModel.goToMainFragment.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                findNavController().navigate(TestFragmentDirections.actionTestFragmentToMainFragment())
                viewModel.goToMainFragmentDone()
            }
        })

        val types = arrayOf(
            getString(R.string.exam),
            getString(R.string.test),
            getString(R.string.short_test),
            getString(R.string.other)
        )
        val typeAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, types)
        binding.typeText.setAdapter(typeAdapter)

        return binding.root
    }
}