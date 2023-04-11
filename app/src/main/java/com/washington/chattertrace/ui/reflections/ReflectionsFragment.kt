package com.washington.chattertrace.ui.reflections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.washington.chattertrace.databinding.FragmentReflectionsBinding

class ReflectionsFragment : Fragment() {

    private lateinit var recordingsViewModel: ReflectionsViewModel
    private var _binding: FragmentReflectionsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recordingsViewModel =
            ViewModelProvider(this).get(ReflectionsViewModel::class.java)

        _binding = FragmentReflectionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textReflections
        recordingsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}