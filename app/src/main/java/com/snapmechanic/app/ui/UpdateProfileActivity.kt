package com.snapmechanic.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.snapmechanic.app.databinding.ActivityUpdateProfileBinding
import com.snapmechanic.app.model.User
import com.snapmechanic.app.repository.UserRepository
import com.snapmechanic.app.utils.Result
import com.snapmechanic.app.utils.gone
import com.snapmechanic.app.utils.hideKeyboard
import com.snapmechanic.app.utils.toast
import com.snapmechanic.app.utils.visible

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        loadProfile()
        binding.btnSave.setOnClickListener {
            hideKeyboard()
            saveProfile()
        }
        setupDropdowns()
    }

    private fun setupDropdowns() {
        val makes = com.snapmechanic.app.utils.Constants.CAR_DATA.keys.toList()
        val makeAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, makes)
        binding.etCarMake.setAdapter(makeAdapter)

        binding.etCarMake.setOnItemClickListener { _, _, position, _ ->
            val selectedMake = makes[position]
            val models = com.snapmechanic.app.utils.Constants.CAR_DATA[selectedMake] ?: emptyList()
            val modelAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, models)
            binding.etCarModel.setAdapter(modelAdapter)
            binding.etCarModel.text.clear()
        }
    }

    private fun loadProfile() {
        UserRepository.getUserProfile { result ->
            if (result is Result.Success) {
                val u = result.data
                binding.etName.setText(u.name)
                binding.etPhone.setText(u.phone)
                binding.etCarMake.setText(u.carMake, false)
                
                val models = com.snapmechanic.app.utils.Constants.CAR_DATA[u.carMake] ?: emptyList()
                val modelAdapter = android.widget.ArrayAdapter(this@UpdateProfileActivity, android.R.layout.simple_dropdown_item_1line, models)
                binding.etCarModel.setAdapter(modelAdapter)
                
                binding.etCarModel.setText(u.carModel, false)
                binding.etCarReg.setText(u.carRegNumber)
            }
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        if (name.isEmpty()) { binding.tilName.error = "Name required"; return }
        if (phone.isEmpty()) { binding.tilPhone.error = "Phone required"; return }
        binding.tilName.error = null; binding.tilPhone.error = null

        val user = User(
            name = name, phone = phone,
            carMake = binding.etCarMake.text.toString().trim(),
            carModel = binding.etCarModel.text.toString().trim(),
            carRegNumber = binding.etCarReg.text.toString().trim()
        )
        UserRepository.updateUserProfile(user) { result ->
            when (result) {
                is Result.Loading -> { binding.progressBar.visible(); binding.btnSave.isEnabled = false }
                is Result.Success -> { binding.progressBar.gone(); binding.btnSave.isEnabled = true
                    toast("Profile updated!"); finish() }
                is Result.Error -> { binding.progressBar.gone(); binding.btnSave.isEnabled = true
                    toast(result.message) }
            }
        }
    }
}
