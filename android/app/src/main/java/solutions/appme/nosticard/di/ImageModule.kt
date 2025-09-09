package solutions.appme.nosticard.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import solutions.appme.nosticard.data.image.ImageFilterProcessorImpl
import solutions.appme.nosticard.data.image.ImageFilterProcessor

val imageModule = module {
    
    single<ImageFilterProcessor> {
        ImageFilterProcessorImpl(androidContext())
    }
}