package solutions.appme.nosticard.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import solutions.appme.nosticard.data.repository.*
import solutions.appme.nosticard.utils.ImageCropUtils
import solutions.appme.nosticard.utils.ImageCropUtilsImpl

val dataModule = module {
    
    // Utilities
    single<ImageCropUtils> {
        ImageCropUtilsImpl()
    }
    
    // Storage Repository
    single<StorageRepository> {
        StorageRepositoryImpl(
            postcardDao = get()
        )
    }
    
    // Image Repository
    single<ImageRepository> {
        ImageRepositoryImpl(
            context = androidContext(),
            imageCropUtils = get()
        )
    }
    
    // Billing Repository
    single<BillingRepository> {
        BillingRepositoryImpl(
            context = androidContext()
        )
    }
    
    // Share Repository
    single<ShareRepository> {
        ShareRepositoryImpl(
            context = androidContext()
        )
    }
}