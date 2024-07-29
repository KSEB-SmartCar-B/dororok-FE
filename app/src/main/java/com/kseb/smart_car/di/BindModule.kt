package com.kseb.smart_car.di

import com.kseb.smart_car.data.datasource.AuthDataSource
import com.kseb.smart_car.data.datasource.KakaoDataSource
import com.kseb.smart_car.data.datasourceImpl.AuthDataSourceImpl
import com.kseb.smart_car.data.datasourceImpl.KakaoDataSourceImpl
import com.kseb.smart_car.data.repositoryImpl.AuthRepositoryImpl
import com.kseb.smart_car.data.repositoryImpl.KakaoRepositoryImpl
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.domain.repository.KakaoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindKakaoRepository(kakaoRepositoryImpl: KakaoRepositoryImpl):KakaoRepository

    @Binds
    @Singleton
    abstract fun provideAuthDataSource(authDataSourceImpl: AuthDataSourceImpl): AuthDataSource

    @Binds
    @Singleton
    abstract fun provideKakaoDataSource(kakaoDataSourceImpl: KakaoDataSourceImpl):KakaoDataSource

}