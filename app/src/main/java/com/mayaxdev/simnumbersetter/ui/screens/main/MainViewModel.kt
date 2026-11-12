package com.mayaxdev.simnumbersetter.ui.screens.main

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayaxdev.simnumbersetter.R
import com.mayaxdev.simnumbersetter.repositories.PermissionRepository
import com.mayaxdev.simnumbersetter.repositories.RootRepository
import com.mayaxdev.simnumbersetter.repositories.ServiceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception

abstract class MainViewModel : ViewModel() {

    abstract val state: StateFlow<State>
    abstract val number: StateFlow<String?>
    abstract val selectedSlot: StateFlow<Int>
    abstract val phoneCount: StateFlow<Int>
    abstract val isRooted: StateFlow<Boolean>
    abstract val isHookConnected: StateFlow<Boolean>

    abstract fun onNumberChanged(newNumber: String)
    abstract fun onSlotSelected(slotIndex: Int)
    abstract fun onSaveClicked()
    abstract fun onRestartTelephonyClicked()
    abstract fun onMenuItemClicked(context: Context, id: Int)
    abstract fun openTelegram(context: Context)
    abstract fun openDevTelegram(context: Context)
    abstract fun openGitHubReleases(context: Context)
    abstract fun grantAllPermissionsViaRoot()
    abstract suspend fun uninstallLegacyAppViaRoot(): Boolean

    sealed class State {
        data class Loading(val loadType: LoadType) : State()
        data class Loaded(internal val number: String) : State()
        data class Error(val errorType: ErrorType) : State()
    }

    enum class ErrorType(@StringRes val messageRes: Int) {
        NO_ROOT(R.string.error_no_root),
        NO_PERMISSION(R.string.error_no_permission),
        NO_XPOSED(R.string.error_no_xposed),
        SAVE_FAILED(R.string.error_save_failed)
    }

    enum class LoadType(@StringRes val contentRes: Int) {
        LOADING(R.string.loading_loading),
        SAVING(R.string.loading_saving)
    }

}

class MainViewModelImpl(
    private val rootRepository: RootRepository,
    private val permissionRepository: PermissionRepository,
    private val serviceRepository: ServiceRepository
) : MainViewModel() {

    companion object {
        const val URL_TELEGRAM = "https://t.me/MayaXDev"
        const val URL_TELEGRAM_DEV = "https://t.me/Itcalldeveloper"
        const val URL_TELEGRAM_SUPPORT = "https://t.me/MayaXDev"
        const val URL_GITHUB_RELEASES = "https://github.com/MayaXDev/SIMNumberSetter-Pro/releases"
    }

    private val customNumber = MutableStateFlow<String?>(null)
    override val selectedSlot = MutableStateFlow(0)
    override val phoneCount = MutableStateFlow(1)
    override val isRooted = MutableStateFlow(false)
    override val isHookConnected = MutableStateFlow(false)

    override val state = MutableStateFlow<State>(State.Loading(LoadType.LOADING))
    override val number = combine(customNumber, state) { n, s ->
        when {
            n != null -> n
            s is State.Loaded -> s.number
            else -> null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override fun onNumberChanged(newNumber: String) {
        viewModelScope.launch {
            customNumber.emit(newNumber)
        }
    }

    override fun onSlotSelected(slotIndex: Int) {
        viewModelScope.launch {
            selectedSlot.emit(slotIndex)
            customNumber.emit(null)
            load()
        }
    }

    override fun onSaveClicked() {
        save(number.value ?: "")
    }

    override fun onRestartTelephonyClicked() {
        viewModelScope.launch {
            state.emit(State.Loading(LoadType.LOADING))
            rootRepository.restartTelephony()
            delay(2500L)
            load()
        }
    }

    override fun openTelegram(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_TELEGRAM)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            // Fallback to browser
        }
    }

    override fun openDevTelegram(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_TELEGRAM_DEV)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            // Fallback to browser
        }
    }

    override fun openGitHubReleases(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_GITHUB_RELEASES)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            // Fallback to browser
        }
    }

    override fun grantAllPermissionsViaRoot() {
        viewModelScope.launch {
            rootRepository.runRootCommand(
                "pm grant com.mayaxdev.simnumbersetter android.permission.READ_PHONE_STATE; " +
                "pm grant com.mayaxdev.simnumbersetter android.permission.READ_PHONE_NUMBERS; " +
                "pm grant com.mayaxdev.simnumbersetter android.permission.DUMP"
            )
        }
    }

    override suspend fun uninstallLegacyAppViaRoot(): Boolean {
        return try {
            val res = rootRepository.runRootCommand("pm uninstall com.kieronquinn.app.simnumbersetter")
            res.isSuccess
        } catch (t: Throwable) {
            false
        }
    }

    override fun onMenuItemClicked(context: Context, id: Int) {
        when(id) {
            R.id.menu_telegram -> openTelegram(context)
            R.id.menu_telegram_dev -> openDevTelegram(context)
            R.id.menu_telegram_support -> openTelegram(context)
            R.id.menu_check_updates -> openGitHubReleases(context)
            R.id.menu_restart_telephony -> onRestartTelephonyClicked()
            else -> return
        }
    }

    private fun load() = viewModelScope.launch {
        state.emit(State.Loading(LoadType.LOADING))
        val rooted = try {
            rootRepository.isRooted()
        } catch (t: Throwable) {
            false
        }
        isRooted.emit(rooted)
        
        val slot = selectedSlot.value

        // Try primary path: LSPosed Xposed Hook via TelephonyDebugService
        var num: String? = null
        try {
            num = serviceRepository.runWithService { service ->
                isHookConnected.emit(true)
                try {
                    phoneCount.emit(service.phoneCount)
                } catch (t: Throwable) {
                    phoneCount.emit(2)
                }
                try {
                    service.getLine1NumberForSlot(slot)
                } catch (t: Throwable) {
                    service.line1Number
                }
            }
        } catch (e: Exception) {
            num = null
        }

        // Secondary fallback path: Direct Root Telephony Provider query
        if (num == null && rooted) {
            isHookConnected.emit(false)
            phoneCount.emit(2)
            num = rootRepository.getLine1NumberViaRoot(slot)
        }

        if (num == null) {
            isHookConnected.emit(false)
            state.emit(State.Error(ErrorType.NO_XPOSED))
            return@launch
        }
        state.emit(State.Loaded(num))
    }

    private fun save(number: String) = viewModelScope.launch {
        state.emit(State.Loading(LoadType.SAVING))
        val slot = selectedSlot.value
        val rooted = isRooted.value

        var success: Boolean? = null

        // Try primary path: LSPosed Hook
        try {
            success = serviceRepository.runWithService { service ->
                try {
                    service.setLine1NumberForSlot(slot, number, null)
                } catch (t: Throwable) {
                    service.setLine1Number(number, null)
                }
            }
        } catch (e: Exception) {
            success = null
        }

        // Secondary fallback path: Direct Root Telephony Content Provider Update
        if (success == null && rooted) {
            success = rootRepository.setLine1NumberViaRoot(slot, number)
            if (success) {
                // Kick telephony cache so changes take effect
                rootRepository.restartTelephony()
            }
        }

        if (success == null) {
            state.emit(State.Error(ErrorType.NO_XPOSED))
            return@launch
        }

        customNumber.emit(null)
        delay(1200L)
        if (success) {
            load()
        } else {
            state.emit(State.Error(ErrorType.SAVE_FAILED))
        }
    }

    init {
        load()
    }

    override fun onCleared() {
        serviceRepository.unbindServiceIfNeeded()
        super.onCleared()
    }

}
