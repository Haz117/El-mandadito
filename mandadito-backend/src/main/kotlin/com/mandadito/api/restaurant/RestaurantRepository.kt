package com.mandadito.api.restaurant

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RestaurantRepository : JpaRepository<Restaurant, Long> {

    fun findByBusinessId(businessId: Long): List<Restaurant>

    @Query(value = """
        SELECT r.* FROM restaurants r
        INNER JOIN businesses b ON r.business_id = b.id
        WHERE b.status = 'APPROVED'
        AND r.status = 'ACTIVE'
        ORDER BY r.rating DESC
    """, nativeQuery = true)
    fun findAllApproved(): List<Restaurant>

    @Query(value = """
        SELECT r.* FROM restaurants r
        INNER JOIN businesses b ON r.business_id = b.id
        WHERE b.status = 'APPROVED'
        AND r.status = 'ACTIVE'
        AND LOWER(r.category) = LOWER(:category)
        ORDER BY r.rating DESC
    """, nativeQuery = true)
    fun findApprovedByCategory(@Param("category") category: String): List<Restaurant>

    @Query(value = """
        SELECT r.* FROM restaurants r
        INNER JOIN businesses b ON r.business_id = b.id
        WHERE b.status = 'APPROVED'
        AND r.status = 'ACTIVE'
        AND LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
        ORDER BY r.rating DESC
    """, nativeQuery = true)
    fun searchApproved(@Param("search") search: String): List<Restaurant>

    @Query(value = """
        SELECT r.* FROM restaurants r
        INNER JOIN businesses b ON r.business_id = b.id
        WHERE r.id = :id
        AND b.status = 'APPROVED'
        AND r.status = 'ACTIVE'
    """, nativeQuery = true)
    fun findApprovedById(@Param("id") id: Long): Restaurant?

    @Query(value = """
        SELECT r.* FROM restaurants r
        INNER JOIN businesses b ON r.business_id = b.id
        WHERE b.status = 'APPROVED'
        AND r.status = 'ACTIVE'
        AND (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) *
            cos(radians(r.longitude) - radians(:lng)) +
            sin(radians(:lat)) * sin(radians(r.latitude)))) < :radiusKm
        ORDER BY r.rating DESC
    """, nativeQuery = true)
    fun findNearby(
        @Param("lat") lat: Double,
        @Param("lng") lng: Double,
        @Param("radiusKm") radiusKm: Double = 10.0
    ): List<Restaurant>
}
