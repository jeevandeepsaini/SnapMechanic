package com.snapmechanic.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snapmechanic.app.R
import com.snapmechanic.app.databinding.ItemGarageCardBinding
import com.snapmechanic.app.model.Garage
import com.snapmechanic.app.utils.toDistanceString
import com.snapmechanic.app.utils.toRatingString

/**
 * GarageAdapter — connects a list of Garage objects to the RecyclerView in HomeActivity.
 *
 * How RecyclerView + Adapter works:
 *   1. RecyclerView asks "how many items?" → getItemCount()
 *   2. For each item, RecyclerView asks "make a view for this" → onCreateViewHolder()
 *   3. RecyclerView asks "fill in item #5" → onBindViewHolder(holder, position)
 *
 * ViewBinding (ItemGarageCardBinding) auto-generates references to every view
 * in item_garage_card.xml, so we don't need findViewById().
 */
class GarageAdapter(
    private val context: Context,
    private val onGarageClick: (Garage) -> Unit  // Called when user taps a card
) : RecyclerView.Adapter<GarageAdapter.GarageViewHolder>() {

    // The current list of garages being displayed
    private val garages = mutableListOf<Garage>()

    // ViewHolder — holds references to views for ONE garage card
    // Using ViewBinding so all view references are type-safe
    inner class GarageViewHolder(val binding: ItemGarageCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GarageViewHolder {
        // Inflate the card layout and wrap it in a ViewHolder
        val binding = ItemGarageCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GarageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GarageViewHolder, position: Int) {
        val garage = garages[position]
        val binding = holder.binding

        // Fill in each view with garage data
        binding.tvGarageName.text = garage.name
        binding.tvRating.text = garage.rating.toRatingString()
        binding.tvDistance.text = garage.distanceKm.toDistanceString()
        binding.tvAddress.text = garage.address

        // Show Open/Closed badge with correct color
        if (garage.isOpen) {
            binding.tvStatus.text = "● Open"
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_open))
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_open)
        } else {
            binding.tvStatus.text = "● Closed"
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_closed))
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_closed)
        }

        // Load garage image using Glide (handles download, caching, placeholder)
        Glide.with(context)
            .load(garage.imageUrl)
            .placeholder(R.drawable.ic_garage_placeholder)
            .error(R.drawable.ic_garage_placeholder)
            .centerCrop()
            .into(binding.ivGarageImage)

        // Set up click listener — when user taps the card, call onGarageClick
        binding.root.setOnClickListener {
            onGarageClick(garage)
        }
    }

    override fun getItemCount(): Int = garages.size

    /**
     * Add more garages (for pagination — append new page to existing list)
     */
    fun addGarages(newGarages: List<Garage>) {
        val startPosition = garages.size
        garages.addAll(newGarages)
        // Only notify about the NEW items, not the whole list (efficient)
        notifyItemRangeInserted(startPosition, newGarages.size)
    }

    /**
     * Replace entire list (for search/filter results)
     */
    fun submitList(newGarages: List<Garage>) {
        garages.clear()
        garages.addAll(newGarages)
        notifyDataSetChanged()
    }

    /**
     * Get all garages currently shown (used for filtering)
     */
    fun getAllGarages(): List<Garage> = garages.toList()

    /**
     * Clear all garages (when refreshing)
     */
    fun clearGarages() {
        garages.clear()
        notifyDataSetChanged()
    }
}
