package test.com.yitexam.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import test.com.yitexam.api.PixabayService
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkServicesModule {

    @Singleton
    @Provides
    fun providePixabayService(retrofit: Retrofit): PixabayService {
        return retrofit.create(PixabayService::class.java)
    }
}