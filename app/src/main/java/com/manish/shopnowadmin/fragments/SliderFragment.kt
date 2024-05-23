package com.manish.shopnowadmin.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.manish.shopnowadmin.R
import com.manish.shopnowadmin.databinding.FragmentSliderBinding
import java.util.UUID

class SliderFragment : Fragment(R.layout.fragment_slider) {

    private lateinit var binding: FragmentSliderBinding
    private var imageUrl: Uri? = null
    private lateinit var dialog: Dialog

    private val launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK)  { // This means we got an image
            imageUrl = it.data!!.data
            binding.sliderImageView.setImageURI(imageUrl)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSliderBinding.bind(view)

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)

        binding.apply {
            sliderImageView.setOnClickListener {
                val intent = Intent("android.intent.action.GET_CONTENT")
                intent.type = "image/*"
                launchGalleryActivity.launch(intent)
            }
            uploadSliderImageButton.setOnClickListener {
                if (imageUrl != null) {
                    uploadImage(imageUrl!!)
                } else {
                    Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // this function will add this image item in the slider collections
    private fun uploadImage(uri: Uri) {
        dialog.show()
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val refStorage = FirebaseStorage.getInstance().reference.child("sliders/$fileName")
        refStorage.putFile(uri)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    storeData(image.toString())
                }
            }
    }

    // this function will upload the image to firebase storage
    private fun storeData(image: String) {
        val db = Firebase.firestore

        val data = hashMapOf<String, Any>(
            "img" to image
        )

        db.collection("sliders").add(data)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Slider Image Updated", Toast.LENGTH_SHORT).show()
                binding.sliderImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.image_preview))
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }
}