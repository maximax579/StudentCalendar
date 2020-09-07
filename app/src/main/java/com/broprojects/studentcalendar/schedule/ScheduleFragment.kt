package com.broprojects.studentcalendar.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.broprojects.studentcalendar.R
import com.broprojects.studentcalendar.ToolbarActivity
import com.broprojects.studentcalendar.database.CalendarDatabase
import com.broprojects.studentcalendar.databinding.FragmentScheduleBinding
import com.broprojects.studentcalendar.helpers.*

class ScheduleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentScheduleBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_schedule, container, false
        )

        val args = ScheduleFragmentArgs.fromBundle(requireArguments())
        val database = CalendarDatabase.getInstance(requireContext())

        val dao = database.schedulesTableDao
        val coursesDao = database.coursesTableDao
        val peopleDao = database.peopleTableDao

        val viewModelFactory =
            ScheduleViewModelFactory(requireActivity(), dao, coursesDao, peopleDao, args.scheduleId?.toLong())
        val viewModel = ViewModelProvider(this, viewModelFactory)[ScheduleViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // If user is updating data, change action bar title and fill text fields
        if (args.scheduleId != null) {
            (activity as ToolbarActivity).setActionBarText(R.string.update_schedule)
        }

        viewModel.model.observe(viewLifecycleOwner, {
            binding.whenText.setText(it.whenTime?.toTimeString(requireContext()))
            binding.startText.setText(it.startDate?.toDateString(requireContext()))
            binding.endText.setText(it.endDate?.toDateString(requireContext()))
            viewModel.loadCourseAndPersonName()
        })

        // If model LiveData has already been loaded, load course and person from database
        viewModel.selectedCourse.observe(viewLifecycleOwner, {
            if (it != null) {
                binding.courseText.setText(it.name, false)
            }
        })

        viewModel.selectedPerson.observe(viewLifecycleOwner, {
            if (it != null) {
                binding.personText.setText(it.toString(), false)
            }
        })

        // Set app color theme on views
        viewModel.colorStateList.observe(viewLifecycleOwner, {
            binding.saveButton.backgroundTintList = it
            binding.typeTextLayout.setBoxStrokeColorStateList(it)
            binding.whenTextLayout.setBoxStrokeColorStateList(it)
            binding.startTextLayout.setBoxStrokeColorStateList(it)
            binding.endTextLayout.setBoxStrokeColorStateList(it)
            binding.personTextLayout.setBoxStrokeColorStateList(it)
            binding.locationTextLayout.setBoxStrokeColorStateList(it)
            binding.infoTextLayout.setBoxStrokeColorStateList(it)
        })

        binding.typeText.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                resources.getStringArray(R.array.schedule_array)
            )
        )

        // Load courses dropdown items and set list in adapter
        viewModel.coursesList.observe(viewLifecycleOwner, { dropdownList ->
            binding.courseText.setAdapter(
                ValueAdapter(requireContext(), dropdownList.map { it.toValueDropdownItem() }.toTypedArray())
            )
            binding.courseText.setOnItemClickListener { adapterView, _, position, _ ->
                viewModel.setCourse((adapterView.getItemAtPosition(position) as ValueDropdownItem).value)
            }
        })

        // Load people dropdown items and set list in adapter
        viewModel.peopleList.observe(viewLifecycleOwner, { dropdownList ->
            binding.personText.setAdapter(
                ValueAdapter(requireContext(), dropdownList.map { it.toValueDropdownItem() }.toTypedArray())
            )
            binding.personText.setOnItemClickListener { adapterView, _, position, _ ->
                viewModel.setPerson((adapterView.getItemAtPosition(position) as ValueDropdownItem).value)
            }
        })

        binding.startText.setOnClickListener {
            activity?.datePickerDialog {
                viewModel.setStartDate(it)
                binding.startText.setText(it.toDateString(requireContext()))
            }
        }

        binding.endText.setOnClickListener {
            activity?.datePickerDialog {
                viewModel.setEndDate(it)
                binding.endText.setText(it.toDateString(requireContext()))
            }
        }

        binding.whenText.setOnClickListener {
            activity?.timePickerDialog {
                viewModel.setWhenTime(it)
                binding.whenText.setText(it.toTimeString(requireContext()))
            }
        }

        binding.saveButton.setOnClickListener {
            // Validate input fields
            val courseEmpty = validateEmpty(this, binding.courseTextLayout, binding.courseText)
            val whenEmpty = validateEmpty(this, binding.whenTextLayout, binding.whenText)
            val startEmpty = validateEmpty(this, binding.startTextLayout, binding.startText)
            val endEmpty = validateEmpty(this, binding.endTextLayout, binding.endText)

            if (courseEmpty && whenEmpty && startEmpty && endEmpty) {
                viewModel.saveData()
            }
        }

        viewModel.goToMainFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                findNavController().navigate(ScheduleFragmentDirections.actionScheduleFragmentToMainFragment())
                viewModel.goToMainFragmentDone()
            }
        })

        return binding.root
    }
}