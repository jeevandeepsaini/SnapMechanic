package com.snapmechanic.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.snapmechanic.app.R
import com.snapmechanic.app.databinding.ActivityRequestServiceBinding
import com.snapmechanic.app.model.Booking
import com.snapmechanic.app.repository.AuthRepository
import com.snapmechanic.app.repository.UserRepository
import com.snapmechanic.app.utils.Result
import com.snapmechanic.app.utils.TimeValidator
import com.snapmechanic.app.utils.gone
import com.snapmechanic.app.utils.hideKeyboard
import com.snapmechanic.app.utils.toast
import com.snapmechanic.app.utils.visible
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * RequestServiceActivity — the booking form screen.
 *
 * Pre-fills vehicle and customer info from the user's Firestore profile.
 * Lets user pick date and time using system DatePickerDialog/TimePickerDialog.
 * Validates: services selected, date and time required, time within garage hours.
 * On submit: saves a Booking document to Firestore → shows success dialog.
 *
 * Data received from GarageDetailActivity via Intent extras.
 */
class RequestServiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestServiceBinding

    // Garage info received from previous screen
    private var garageId = ""
    private var garageName = ""
    private var garagePhone = ""
    private var openTime = ""
    private var closeTime = ""
    private var availableServices = listOf<String>()

    // User selection state
    private var selectedDate = ""
    private var selectedTime = ""
    private val selectedServices = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Read garage data from intent
        garageId = intent.getStringExtra("garageId") ?: ""
        garageName = intent.getStringExtra("garageName") ?: ""
        garagePhone = intent.getStringExtra("garagePhone") ?: ""
        openTime = intent.getStringExtra("openTime") ?: ""
        closeTime = intent.getStringExtra("closeTime") ?: ""
        availableServices = intent.getStringArrayListExtra("services") ?: listOf()

        binding.btnBack.setOnClickListener { finish() }

        // Load user profile to pre-fill the form
        loadUserProfile()
        populateServiceChips()
        setupDateTimePickers()
        setupSubmitButton()
    }

    /**
     * Load the user's saved profile from Firestore and pre-fill the form fields.
     * Fields are non-editable — user updates them in the Profile section.
     */
    private fun loadUserProfile() {
        UserRepository.getUserProfile { result ->
            when (result) {
                is Result.Success -> {
                    val user = result.data
                    binding.etCarMake.setText(user.carMake)
                    binding.etCarModel.setText(user.carModel)
                    binding.etCarReg.setText(user.carRegNumber)
                    binding.etName.setText(user.name)
                    binding.etPhone.setText(user.phone)
                }
                is Result.Error -> {
                    // If profile fetch fails, show empty fields — user can see the error
                    toast("Could not load profile: ${result.message}")
                }
                is Result.Loading -> {}
            }
        }
    }

    /**
     * Create a Chip for each service the garage offers.
     * Multiple chips can be selected → stored in selectedServices set.
     */
    private fun populateServiceChips() {
        binding.chipGroupServices.removeAllViews()
        availableServices.forEach { service ->
            val chip = Chip(this).apply {
                text = service
                isCheckable = true  // Toggles selection on tap
                setChipBackgroundColorResource(R.color.bg_surface)
                setTextColor(ContextCompat.getColor(this@RequestServiceActivity, R.color.text_secondary))
                chipStrokeWidth = 1f
                setChipStrokeColorResource(R.color.stroke)

                // When chip is checked/unchecked, update selectedServices set
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedServices.add(service)
                        // Highlight selected chip with orange
                        setChipBackgroundColorResource(R.color.brand_orange)
                        setTextColor(ContextCompat.getColor(this@RequestServiceActivity, R.color.white))
                    } else {
                        selectedServices.remove(service)
                        setChipBackgroundColorResource(R.color.bg_surface)
                        setTextColor(ContextCompat.getColor(this@RequestServiceActivity, R.color.text_secondary))
                    }
                }
            }
            binding.chipGroupServices.addView(chip)
        }
    }

    private fun setupDateTimePickers() {
        // Date picker — opens system calendar dialog
        binding.btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    selectedDate = String.format("%02d/%02d/%04d", day, month + 1, year)
                    binding.tvDate.text = selectedDate
                    binding.tvDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                // Don't allow selecting past dates
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        // Time picker — opens system clock dialog
        binding.btnPickTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    selectedTime = String.format("%02d:%02d", hour, minute)
                    binding.tvTime.text = selectedTime
                    binding.tvTime.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true  // 24-hour clock
            ).show()
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (!validateForm()) return@setOnClickListener
            hideKeyboard()
            submitBooking()
        }
    }

    private fun validateForm(): Boolean {
        if (selectedServices.isEmpty()) {
            toast("Please select at least one service")
            return false
        }
        if (selectedDate.isEmpty()) {
            toast("Please select a date")
            return false
        }
        if (selectedTime.isEmpty()) {
            toast("Please select a time")
            return false
        }

        // Validate selected time is within garage working hours
        val timeCheck = TimeValidator.isWithinWorkingHours(selectedTime, openTime, closeTime)
        if (!timeCheck.isValid) {
            toast(timeCheck.message)
            return false
        }

        return true
    }

    private fun submitBooking() {
        val booking = Booking(
            garageId = garageId,
            garageName = garageName,
            garagePhone = garagePhone,
            services = selectedServices.toList(),
            date = selectedDate,
            time = selectedTime,
            issue = binding.etIssue.text.toString().trim(),
            carMake = binding.etCarMake.text.toString().trim(),
            carModel = binding.etCarModel.text.toString().trim(),
            carRegNumber = binding.etCarReg.text.toString().trim(),
            customerName = binding.etName.text.toString().trim(),
            customerPhone = binding.etPhone.text.toString().trim()
        )

        UserRepository.createBooking(booking) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSubmit.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressBar.gone()
                    showSuccessDialog()
                }
                is Result.Error -> {
                    binding.progressBar.gone()
                    binding.btnSubmit.isEnabled = true
                    toast("Booking failed: ${result.message}")
                }
            }
        }
    }

    /**
     * Shows a confirmation dialog after successful booking.
     * Displays all booking details as a summary.
     */
    private fun showSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_booking_confirmed, null)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .show()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<android.widget.Button>(R.id.btnViewBookings).setOnClickListener {
            dialog.dismiss()
            finish()
        }
    }
}
