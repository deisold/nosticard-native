package solutions.appme.nosticard.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import solutions.appme.nosticard.features.home.viewmodel.HomeViewModel
import solutions.appme.nosticard.features.editor.viewmodel.EditorViewModel
import solutions.appme.nosticard.features.preview.viewmodel.PreviewViewModel
import solutions.appme.nosticard.features.mycards.viewmodel.MyCardsViewModel
import solutions.appme.nosticard.features.settings.viewmodel.SettingsViewModel

val viewModelModule = module {
    
    viewModel {
        HomeViewModel(
            getPostcardsUseCase = get(),
            deletePostcardUseCase = get(),
            duplicatePostcardUseCase = get()
        )
    }
    
    viewModel { parameters ->
        EditorViewModel(
            postcardId = parameters.getOrNull(),
            getPostcardUseCase = get(),
            imageRepository = get(),
            applyFilterUseCase = get(),
            savePostcardUseCase = get()
        )
    }
    
    viewModel { parameters ->
        PreviewViewModel(
            postcardId = parameters.get(),
            exportPostcardUseCase = get(),
            sharePostcardUseCase = get()
        )
    }
    
    viewModel {
        MyCardsViewModel(
            getPostcardsUseCase = get(),
            deletePostcardUseCase = get()
        )
    }
    
    viewModel {
        SettingsViewModel(
            billingRepository = get()
        )
    }
}