package com.snapmechanic.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.snapmechanic.app.R
import com.snapmechanic.app.databinding.ActivityGarageDetailBinding
import com.snapmechanic.app.model.Garage
import com.snapmechanic.app.repository.GarageRepository
import com.snapmechanic.app.utils.Constants
import com.snapmechanic.app.utils.Result
import com.snapmechanic.app.utils.gone
import com.snapmechanic.app.utils.toast
import com.snapmechanic.app.utils.toRatingString
import com.snapmechanic.app.utils.visible

/**
 * GarageDetailActivity — shows full details of a selected garage.
 *
 * Receives the garage ID from HomeActivity via Intent.putExtra().
 * Calls GarageRepository.getGarageById() to fetch the specific garage.
 * Dynamically adds service Chips to the ChipGroup.
 * The "Request Service" button passes the full Garage object to RequestServiceActivity.
 *
 * Note: We pass the Garage object as a JSON string via Gson because
 * Android Intent extras can't directly hold complex objects.
 */
class GarageDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGarageDetailBinding
    private var currentGarage: Garage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGarageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        // Get the garage ID that HomeActivity passed to us
        val garageId = intent.getStringExtra(Constants.EXTRA_GARAGE_ID) ?: run {
            toast("Garage not found")
            finish()
            return
        }

        loadGarageDetails(garageId)
    }

    private fun loadGarageDetails(id: String) {
        GarageRepository.getGarageById(id) { result ->
            when (result) {
                is Result.Loading -> binding.progressBar.visible()
                is Result.Success -> {
                    binding.progressBar.gone()
                    currentGarage = result.data
                    displayGarage(result.data)
                }
                is Result.Error -> {
                    binding.progressBar.gone()
                    toast(result.message)
                    finish()
                }
            }
        }
    }

    private fun displayGarage(garage: Garage) {
        // Load the hero image
        Glide.with(this)
            .load(garage.imageUrl)
            .placeholder(R.drawable.ic_garage_placeholder)
            .error(R.drawable.ic_garage_placeholder)
            .centerCrop()
            .into(binding.ivGarageImage)

        binding.tvGarageName.text = garage.name
        binding.tvRating.text = garage.rating.toRatingString()
        binding.tvAddress.text = garage.address
        binding.tvPhone.text = garage.phone
        binding.tvWorkingHours.text = "${garage.openTime} – ${garage.closeTime}"

        // Open/Closed status badge
        if (garage.isOpen) {
            binding.tvStatus.text = "● Open"
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_open))
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_open)
        } else {
            binding.tvStatus.text = "● Closed"
            binding.tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_closed))
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_closed)
        }

        // Add a Chip for each service in the list
        binding.chipGroupServices.removeAllViews()
        garage.services.forEach { service ->
            val chip = Chip(this).apply {
                text = service
                isCheckable = false
                setChipBackgroundColorResource(R.color.bg_surface)
                setTextColor(ContextCompat.getColor(this@GarageDetailActivity, R.color.text_secondary))
                chipStrokeWidth = 1f
                setChipStrokeColorResource(R.color.stroke)
            }
            binding.chipGroupServices.addView(chip)
        }

        // CALL button — opens phone dialer
        binding.btnCall.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${garage.phone}"))
            startActivity(dialIntent)
        }

        // REQUEST SERVICE button — pass garage data to RequestServiceActivity
        binding.btnRequestService.setOnClickListener {
            val intent = Intent(this, RequestServiceActivity::class.java).apply {
                putExtra("garageId", garage.id)
                putExtra("garageName", garage.name)
                putExtra("garagePhone", garage.phone)
                putExtra("openTime", garage.openTime)
                putExtra("closeTime", garage.closeTime)
                // Pass services as a string array
                putStringArrayListExtra("services", ArrayList(garage.services))
            }
            startActivity(intent)
        }
    }
}
