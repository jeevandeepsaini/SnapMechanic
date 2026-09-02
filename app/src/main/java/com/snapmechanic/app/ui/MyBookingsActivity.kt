package com.snapmechanic.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.snapmechanic.app.adapter.BookingAdapter
import com.snapmechanic.app.databinding.ActivityMyBookingsBinding
import com.snapmechanic.app.repository.UserRepository
import com.snapmechanic.app.utils.Result
import com.snapmechanic.app.utils.gone
import com.snapmechanic.app.utils.toast
import com.snapmechanic.app.utils.visible

class MyBookingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyBookingsBinding
    private val adapter = BookingAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = adapter

        loadBookings()
    }

    private fun loadBookings() {
        UserRepository.getMyBookings { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visible()
                    binding.layoutEmpty.gone()
                }
                is Result.Success -> {
                    binding.progressBar.gone()
                    if (result.data.isEmpty()) {
                        binding.layoutEmpty.visible()
                    } else {
                        binding.layoutEmpty.gone()
                        adapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    binding.progressBar.gone()
                    binding.layoutEmpty.visible()
                    toast(result.message)
                }
            }
        }
    }
}
