package com.norram.patienter

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.norram.patienter.databinding.FragmentResetBinding

class ResetFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentResetBinding>(inflater,
            R.layout.fragment_reset, container, false)
        val args: ResetFragmentArgs by navArgs()
        binding.congText.text = args.message
        binding.totalTimeText.text = getString(R.string.total).format(args.hours)

        binding.resetButton.setOnClickListener { view: View->
            view.findNavController()
                .navigate(R.id.action_resetFragment_to_setFragment)
        }

        return binding.root
    }
}