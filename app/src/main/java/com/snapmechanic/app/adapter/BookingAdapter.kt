package com.snapmechanic.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snapmechanic.app.databinding.ItemBookingCardBinding
import com.snapmechanic.app.model.Booking

/**
 * BookingAdapter — displays the user's bookings in MyBookingsActivity.
 * Each card shows: garage name, date, time, services booked, car info.
 */
class BookingAdapter : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    private val bookings = mutableListOf<Booking>()

    inner class BookingViewHolder(val binding: ItemBookingCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        val binding = holder.binding

        binding.tvGarageName.text = booking.garageName
        binding.tvBookingDate.text = "📅 ${booking.date} at ${booking.time}"
        binding.tvServices.text = "🔧 ${booking.services.joinToString(", ")}"
        binding.tvCarInfo.text = "🚗 ${booking.carMake} ${booking.carModel} · ${booking.carRegNumber}"
        binding.tvIssue.text = if (booking.issue.isNotBlank()) "📝 ${booking.issue}" else ""
    }

    override fun getItemCount(): Int = bookings.size

    fun submitList(newBookings: List<Booking>) {
        bookings.clear()
        bookings.addAll(newBookings)
        notifyDataSetChanged()
    }
}
