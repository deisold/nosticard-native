package solutions.appme.nosticard

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import solutions.appme.nosticard.di.appModule
import solutions.appme.nosticard.di.dataModule
import solutions.appme.nosticard.di.domainModule
import solutions.appme.nosticard.di.imageModule
import solutions.appme.nosticard.di.viewModelModule

class NostiCardApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@NostiCardApplication)
            modules(
                appModule,
                dataModule,
                domainModule,
                imageModule,
                viewModelModule
            )
        }
    }
}