package solutions.appme.nosticard.di

import org.koin.dsl.module
import solutions.appme.nosticard.features.editor.domain.ApplyFilterUseCase
import solutions.appme.nosticard.features.editor.domain.GetPostcardUseCase
import solutions.appme.nosticard.features.editor.domain.SavePostcardUseCase
import solutions.appme.nosticard.features.home.domain.GetPostcardsUseCase
import solutions.appme.nosticard.features.home.domain.DeletePostcardUseCase
import solutions.appme.nosticard.features.home.domain.DuplicatePostcardUseCase
import solutions.appme.nosticard.features.preview.domain.ExportPostcardUseCase
import solutions.appme.nosticard.features.preview.domain.SharePostcardUseCase

val domainModule = module {
    
    // Home Use Cases
    factory<GetPostcardsUseCase> {
        GetPostcardsUseCase(
            storageRepository = get()
        )
    }
    
    factory<DeletePostcardUseCase> {
        DeletePostcardUseCase(
            storageRepository = get()
        )
    }
    
    factory<DuplicatePostcardUseCase> {
        DuplicatePostcardUseCase(
            storageRepository = get()
        )
    }
    
    // Editor Use Cases
    factory<GetPostcardUseCase> {
        GetPostcardUseCase(
            storageRepository = get()
        )
    }
    
    factory<ApplyFilterUseCase> {
        ApplyFilterUseCase(
            imageFilterProcessor = get()
        )
    }
    
    factory<SavePostcardUseCase> {
        SavePostcardUseCase(
            storageRepository = get()
        )
    }
    
    // Preview Use Cases
    factory<ExportPostcardUseCase> {
        ExportPostcardUseCase(
            imageRepository = get(),
            billingRepository = get()
        )
    }
    
    factory<SharePostcardUseCase> {
        SharePostcardUseCase(
            shareRepository = get()
        )
    }
}