package solutions.appme.nosticard.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import solutions.appme.nosticard.data.repository.*

val dataModule = module {
    
    // Storage Repository
    single<StorageRepository> {
        StorageRepositoryImpl(
            postcardDao = get()
        )
    }
    
    // Image Repository
    single<ImageRepository> {
        ImageRepositoryImpl(
            context = androidContext()
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