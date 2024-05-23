package com.manish.shopnowadmin.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.manish.shopnowadmin.R
import com.manish.shopnowadmin.activities.AllOrdersActivity
import com.manish.shopnowadmin.databinding.FragmentHomeBinding


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        binding.apply {
            addCategoryButton.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_categoryFragment)
            }
            addProductButton.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_productFragment)
            }
            addSliderButton.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_sliderFragment)
            }
//            allOrderDetailsButton.setOnClickListener {
//                startActivity(Intent(requireContext(), AllOrdersActivity::class.java))
//            }
        }
    }

}