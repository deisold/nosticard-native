package solutions.appme.nosticard.di

import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import solutions.appme.nosticard.data.database.NostiCardDatabase

val appModule = module {
    
    // Gson
    single<Gson> { Gson() }
    
    // Database
    single<NostiCardDatabase> {
        NostiCardDatabase.create(androidContext(), get())
    }
    
    // DAO
    single { get<NostiCardDatabase>().postcardDao() }
}