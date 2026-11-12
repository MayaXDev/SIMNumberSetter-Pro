package com.mayaxdev.simnumbersetter

import android.app.Application
import com.mayaxdev.simnumbersetter.repositories.*
import com.mayaxdev.simnumbersetter.ui.screens.main.MainViewModel
import com.mayaxdev.simnumbersetter.ui.screens.main.MainViewModelImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Application: Application() {

    private val repositories = module {
        single<PermissionRepository> { PermissionRepositoryImpl(get(), get()) }
        single<RootRepository> { RootRepositoryImpl() }
        single<ServiceRepository> { ServiceRepositoryImpl(get()) }
    }

    private val viewModels = module {
        viewModel<MainViewModel> { MainViewModelImpl(get(), get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Application)
            modules(repositories, viewModels)
        }
    }

}