package solutions.appme.nosticard.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import solutions.appme.nosticard.data.database.NostiCardDatabase

val appModule = module {
    
    // Database
    single<NostiCardDatabase> {
        NostiCardDatabase.create(androidContext())
    }
    
    // DAO
    single { get<NostiCardDatabase>().postcardDao() }
}