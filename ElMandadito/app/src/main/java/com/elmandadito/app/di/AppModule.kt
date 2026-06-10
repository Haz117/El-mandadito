package com.elmandadito.app.di

import android.content.Context
import com.elmandadito.app.network.repository.AuthRepository
import com.elmandadito.app.network.repository.NotificationNetworkRepository
import com.elmandadito.app.network.repository.OrderNetworkRepository
import com.elmandadito.app.network.repository.RestaurantNetworkRepository
import com.elmandadito.app.network.repository.ReviewNetworkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository =
        AuthRepository(context)

    @Provides
    @Singleton
    fun provideRestaurantRepository(authRepository: AuthRepository): RestaurantNetworkRepository =
        RestaurantNetworkRepository { runBlocking { authRepository.getToken() } }

    @Provides
    @Singleton
    fun provideOrderRepository(authRepository: AuthRepository): OrderNetworkRepository =
        OrderNetworkRepository { runBlocking { authRepository.getToken() } }

    @Provides
    @Singleton
    fun provideNotificationRepository(authRepository: AuthRepository): NotificationNetworkRepository =
        NotificationNetworkRepository { runBlocking { authRepository.getToken() } }

    @Provides
    @Singleton
    fun provideReviewRepository(authRepository: AuthRepository): ReviewNetworkRepository =
        ReviewNetworkRepository { runBlocking { authRepository.getToken() } }
}
