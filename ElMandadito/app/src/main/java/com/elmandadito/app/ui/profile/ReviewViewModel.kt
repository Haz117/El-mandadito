package com.elmandadito.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elmandadito.app.network.repository.ReviewNetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewNetworkRepository
) : ViewModel() {

    fun submitReview(networkOrderId: Long, restaurantRating: Int, comment: String?) {
        if (networkOrderId <= 0L) return
        viewModelScope.launch {
            reviewRepository.create(networkOrderId, restaurantRating, comment)
        }
    }
}
