package com.manish.shopnowadmin.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.manish.shopnowadmin.R
import com.manish.shopnowadmin.adapters.AddProductImageAdapter
import com.manish.shopnowadmin.databinding.FragmentAddProductBinding
import com.manish.shopnowadmin.model.AddProductModel
import com.manish.shopnowadmin.model.CategoryModel
import java.util.UUID

class AddProductFragment : Fragment(R.layout.fragment_add_product) {

    private lateinit var binding: FragmentAddProductBinding
    private lateinit var productImagesList: ArrayList<Uri>
    private lateinit var listImages: ArrayList<String>
    private lateinit var adapter: AddProductImageAdapter
    private var coverImage: Uri? = null
    private lateinit var dialog: Dialog
    private var coverImgUrl: String? = ""
    private lateinit var categoryList: ArrayList<String>

    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK){
            coverImage = it.data!!.data
            binding.productCoverImg.setImageURI(coverImage)
            binding.productCoverImg.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private var launchProductActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK){
            val imageUrl = it.data!!.data
            productImagesList.add(imageUrl!!)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddProductBinding.bind(view)

        productImagesList = ArrayList()
        listImages = ArrayList()

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)

        binding.selectCoverImg.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGalleryActivity.launch(intent)
        }

        binding.selectProductImg.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchProductActivity.launch(intent)
        }

        adapter = AddProductImageAdapter(productImagesList)
        binding.productImgRV.adapter = adapter

        binding.addProduct.setOnClickListener {
            validateProduct()
        }

        getProductCategoryList()
    }

    private fun validateProduct() {
        if (binding.productNameET.text.toString().isEmpty()) {
            binding.productNameET.requestFocus()
            binding.productNameET.error = "Empty"
        } else if (binding.productSPET.text.toString().isEmpty()) {
            binding.productSPET.requestFocus()
            binding.productSPET.error = "Empty"
        } else if (coverImage == null) {
            Toast.makeText(requireContext(), "Please select cover image", Toast.LENGTH_SHORT).show()
        } else if (productImagesList.size < 1) {
            Toast.makeText(requireContext(), "Please select product images", Toast.LENGTH_SHORT).show()
        } else {
            uploadImage()
        }
    }
    private fun uploadImage() {
        dialog.show()
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")

        refStorage.putFile(coverImage!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    coverImgUrl = image.toString()

                    uploadProductImage()
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong with storage", Toast.LENGTH_SHORT).show()
            }
    }

    private var i = 0;
    private fun uploadProductImage() {
        dialog.show()
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")

        refStorage.putFile(productImagesList[i])
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    listImages.add(image!!.toString())
                    if (productImagesList.size == listImages.size) {
                        storeData()
                    } else {
                        i += 1
                        uploadProductImage()
                    }
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong with storage", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData() {

        val db = Firebase.firestore.collection("products")
        val key = db.document().id

        val data = AddProductModel(
            binding.productNameET.text.toString(),
            binding.productDescriptionET.text.toString(),
            coverImgUrl.toString(),
            categoryList[binding.productCategoryDropdown.selectedItemPosition],
            key,
            binding.productMRPET.text.toString(),
            binding.productSPET.text.toString(),
            listImages
        )
        db.document(key).set(data).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Product Added", Toast.LENGTH_SHORT).show()
        }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getProductCategoryList() {

        categoryList = ArrayList()
        Firebase.firestore.collection("categories").get()
            .addOnSuccessListener {
                categoryList.clear()
                for (doc in it.documents) {
                    val data = doc.toObject(CategoryModel::class.java)
                    categoryList.add(data!!.cat!!)
                }
                categoryList.add(0, "Select Category")

                val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item_layout, categoryList)
                binding.productCategoryDropdown.adapter = arrayAdapter
            }
    }
}