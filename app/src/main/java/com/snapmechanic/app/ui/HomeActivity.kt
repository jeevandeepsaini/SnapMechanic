package com.snapmechanic.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snapmechanic.app.adapter.GarageAdapter
import com.snapmechanic.app.databinding.ActivityHomeBinding
import com.snapmechanic.app.model.Garage
import com.snapmechanic.app.repository.GarageRepository
import com.snapmechanic.app.utils.Constants
import com.snapmechanic.app.utils.Result
import com.snapmechanic.app.utils.gone
import com.snapmechanic.app.utils.toast
import com.snapmechanic.app.utils.visible

/**
 * HomeActivity — the main screen showing the paginated list of garages.
 *
 * Key features:
 * 1. Loads garages from mockapi.io with pagination (5 per page)
 * 2. Search bar filters the currently loaded list in real-time
 * 3. Scroll listener loads next page when user reaches bottom
 * 4. SwipeRefresh resets and reloads from page 1
 * 5. Error state shows a Retry button
 *
 * How pagination works:
 *   currentPage starts at 1
 *   When user scrolls to bottom → currentPage++ → load next page → append to list
 *   isLastPage = true when API returns empty list (no more data)
 *   isLoading = true while a page is being fetched (prevents duplicate requests)
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var garageAdapter: GarageAdapter

    // Pagination state variables
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false

    // Store the full unfiltered list for search
    private val allGarages = mutableListOf<Garage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        setupSwipeRefresh()
        setupProfileButton()

        // Load first page on start
        loadGarages()
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        garageAdapter = GarageAdapter(this) { garage ->
            // When a garage card is clicked, open its detail screen
            val intent = Intent(this, GarageDetailActivity::class.java)
            intent.putExtra(Constants.EXTRA_GARAGE_ID, garage.id)
            startActivity(intent)
        }

        binding.rvGarages.layoutManager = layoutManager
        binding.rvGarages.adapter = garageAdapter

        // Scroll listener — detect when user is near the bottom
        binding.rvGarages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Only scroll down triggers loading
                if (dy <= 0) return

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

                // Load next page when 3 items from the bottom
                val isAtBottom = (visibleItemCount + firstVisibleItem) >= (totalItemCount - 3)

                if (!isLoading && !isLastPage && isAtBottom) {
                    currentPage++
                    loadGarages()
                }
            }
        })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterGarages(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterGarages(query: String) {
        if (query.isEmpty()) {
            // Show all garages when search is empty
            garageAdapter.submitList(allGarages)
        } else {
            // Filter by garage name (case-insensitive)
            val filtered = allGarages.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.address.contains(query, ignoreCase = true)
            }
            garageAdapter.submitList(filtered)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            resources.getColor(com.snapmechanic.app.R.color.brand_orange, theme)
        )
        binding.swipeRefresh.setOnRefreshListener {
            // Reset pagination and reload
            currentPage = 1
            isLastPage = false
            allGarages.clear()
            garageAdapter.clearGarages()
            loadGarages()
        }
    }

    private fun setupProfileButton() {
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadGarages() {
        GarageRepository.getGarages(currentPage) { result ->
            when (result) {
                is Result.Loading -> {
                    isLoading = true
                    if (currentPage == 1) {
                        binding.progressBar.visible()
                        binding.layoutError.gone()
                    } else {
                        binding.progressPagination.visible()
                    }
                }
                is Result.Success -> {
                    isLoading = false
                    binding.progressBar.gone()
                    binding.progressPagination.gone()
                    binding.swipeRefresh.isRefreshing = false

                    val newGarages = result.data

                    if (newGarages.isEmpty()) {
                        // No more data — stop paginating
                        isLastPage = true
                    } else {
                        // Add to our master list and update adapter
                        allGarages.addAll(newGarages)
                        garageAdapter.addGarages(newGarages)
                        binding.layoutError.gone()
                    }

                    // Show empty state if no garages at all
                    if (allGarages.isEmpty()) {
                        binding.tvErrorMessage.text = "No garages found"
                        binding.layoutError.visible()
                    }
                }
                is Result.Error -> {
                    isLoading = false
                    binding.progressBar.gone()
                    binding.progressPagination.gone()
                    binding.swipeRefresh.isRefreshing = false

                    if (currentPage == 1) {
                        // First load failed — show error state
                        binding.tvErrorMessage.text = result.message
                        binding.layoutError.visible()
                    } else {
                        // Pagination failed — show toast, don't break the existing list
                        toast("Failed to load more: ${result.message}")
                        currentPage-- // Reset page so retry works
                    }
                }
            }
        }

        // Set up retry button
        binding.btnRetry.setOnClickListener {
            currentPage = 1
            isLastPage = false
            allGarages.clear()
            garageAdapter.clearGarages()
            loadGarages()
        }
    }
}
