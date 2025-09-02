package solutions.appme.nosticard.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import solutions.appme.nosticard.features.home.viewmodel.HomeViewModel
import solutions.appme.nosticard.features.editor.viewmodel.EditorViewModel
import solutions.appme.nosticard.features.preview.viewmodel.PreviewViewModel
import solutions.appme.nosticard.features.mycards.viewmodel.MyCardsViewModel
import solutions.appme.nosticard.features.settings.viewmodel.SettingsViewModel

val viewModelModule = module {
    
    viewModel<HomeViewModel> {
        HomeViewModel(
            getPostcardsUseCase = get(),
            deletePostcardUseCase = get(),
            duplicatePostcardUseCase = get()
        )
    }
    
    viewModel<EditorViewModel> {
        EditorViewModel(
            applyFilterUseCase = get(),
            savePostcardUseCase = get()
        )
    }
    
    viewModel<PreviewViewModel> {
        PreviewViewModel(
            exportPostcardUseCase = get(),
            sharePostcardUseCase = get()
        )
    }
    
    viewModel<MyCardsViewModel> {
        MyCardsViewModel(
            getPostcardsUseCase = get(),
            deletePostcardUseCase = get()
        )
    }
    
    viewModel<SettingsViewModel> {
        SettingsViewModel(
            billingRepository = get()
        )
    }
}