package com.mayaxdev.simnumbersetter.ui.screens.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mayaxdev.simnumbersetter.R
import com.mayaxdev.simnumbersetter.databinding.FragmentMainBinding
import com.mayaxdev.simnumbersetter.ui.screens.main.MainViewModel.*
import com.mayaxdev.simnumbersetter.utils.extensions.hideIme
import com.mayaxdev.simnumbersetter.utils.extensions.onChanged
import com.mayaxdev.simnumbersetter.utils.extensions.onClicked
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.NullPointerException

class MainFragment: Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding
        ?: throw NullPointerException("Unable to access binding before onViewCreated or after onDestroyView")

    private val viewModel by viewModel<MainViewModel>()
    private val monet by lazy {
        MonetCompat.getInstance()
    }

    private val bottomPadding by lazy {
        resources.getDimension(R.dimen.margin_16).toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInsets()
        setupState()
        setupToolbar()
        setupNumber()
        setupMonet()
        setupSaveButton()
        setupCloseButton()
        setupInput()
        setupTelegramButton()
        setupPermissions()
        setupDiagnostics()
        setupSlotSelector()
        setupGuide()
        setupTerminal()
        checkAndPromptLegacyPackageUninstall()
    }

    private fun checkAndPromptLegacyPackageUninstall() {
        val legacyPackage = "com.kieronquinn.app.simnumbersetter"
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val isInstalled = try {
                requireContext().packageManager.getPackageInfo(legacyPackage, 0)
                true
            } catch (e: Exception) {
                // If package visibility filter hid it, verify via root pm path
                try {
                    val rootCheck = com.topjohnwu.superuser.Shell.su("pm path $legacyPackage").exec()
                    rootCheck.isSuccess && rootCheck.out.any { it.contains("package:") }
                } catch (t: Throwable) {
                    false
                }
            }

            if (isInstalled && isAdded) {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_legacy_conflict_title)
                    .setMessage(R.string.dialog_legacy_conflict_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.btn_uninstall_legacy) { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                            val rootResult = viewModel.uninstallLegacyAppViaRoot()
                            if (!rootResult) {
                                try {
                                    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                                        data = Uri.parse("package:$legacyPackage")
                                        putExtra(Intent.EXTRA_RETURN_RESULT, true)
                                    }
                                    startActivity(intent)
                                } catch (t: Throwable) {
                                    val intent = Intent(Intent.ACTION_DELETE).apply {
                                        data = Uri.parse("package:$legacyPackage")
                                    }
                                    startActivity(intent)
                                }
                            } else {
                                android.widget.Toast.makeText(
                                    requireContext(),
                                    "Purana conflicting app successfully uninstall ho gaya!",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton(R.string.btn_ignore_legacy, null)
                    .show()
            }
        }
    }

    private fun setupPermissions() {
        updatePermissionState()
        binding.includeMainLoaded.includeMainLoadedCardPerms.btnAutoGrantPerms.setOnClickListener {
            autoGrantPermissions()
        }
    }

    private fun updatePermissionState() = with(binding.includeMainLoaded.includeMainLoadedCardPerms) {
        val ctx = context ?: return@with
        val hasPhone = androidx.core.content.ContextCompat.checkSelfPermission(
            ctx,
            android.Manifest.permission.READ_PHONE_STATE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        statusPermPhone.text = if (hasPhone) "Granted" else "Required"
        statusPermPhone.setTextColor(
            androidx.core.content.ContextCompat.getColor(
                ctx,
                if (hasPhone) R.color.neon_green else R.color.neon_red
            )
        )
        iconPermPhone.setImageResource(if (hasPhone) R.drawable.ic_check_circle else R.drawable.ic_error)
        iconPermPhone.imageTintList = android.content.res.ColorStateList.valueOf(
            androidx.core.content.ContextCompat.getColor(
                ctx,
                if (hasPhone) R.color.neon_green else R.color.neon_red
            )
        )

        statusPermDump.text = "Active"
        statusPermDump.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.neon_green))
        iconPermDump.setImageResource(R.drawable.ic_check_circle)
        iconPermDump.imageTintList = android.content.res.ColorStateList.valueOf(
            androidx.core.content.ContextCompat.getColor(ctx, R.color.neon_green)
        )
    }

    private fun autoGrantPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_PHONE_NUMBERS
        )
        requestPermissions(permissions, 201)

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.grantAllPermissionsViaRoot()
            updatePermissionState()
            android.widget.Toast.makeText(requireContext(), "Pro Privileges & Permissions Synchronized!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        updatePermissionState()
    }

    private fun setupTerminal() {
        with(binding.includeMainLoaded.includeMainLoadedCardTerminal) {
            btnTerminalExec.setOnClickListener {
                val cmd = inputTerminalCmd.text?.toString() ?: ""
                if (cmd.isNotBlank()) {
                    viewModel.executeTerminalCommand(cmd)
                    inputTerminalCmd.setText("")
                }
            }

            btnCmdQuerySiminfo.setOnClickListener {
                viewModel.executeTerminalCommand("content query --uri content://telephony/siminfo --projection sim_id:number")
            }

            btnCmdCheckRoot.setOnClickListener {
                viewModel.executeTerminalCommand("id; which su")
            }

            btnCmdTelephonyLogs.setOnClickListener {
                viewModel.executeTerminalCommand("logcat -d -s TelephonyProvider,TelephonyRegistry,com.android.phone -t 25")
            }

            btnCmdClearLogs.setOnClickListener {
                viewModel.clearTerminalLogs()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                viewModel.terminalLogs.collect { logs ->
                    textTerminalLogs.text = logs
                    terminalScrollView.post {
                        terminalScrollView.fullScroll(View.FOCUS_DOWN)
                    }
                }
            }
        }
    }

    private fun setupGuide() {
        binding.includeMainLoaded.includeMainLoadedCardInfo.guideTextView.text =
            androidx.core.text.HtmlCompat.fromHtml(
                getString(R.string.guide_content),
                androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
            )
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainToolbar) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = topInset)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.includeMainLoaded.root) { view, insets ->
            val bottomInset = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            ).bottom
            view.updatePadding(bottom = bottomPadding + bottomInset)
            insets
        }
    }

    private fun setupMonet() = with(binding) {
        // Retain cyber pro theme surfaces
    }

    private fun setupToolbar() = with(binding.mainToolbar) {
        MenuInflater(requireContext()).inflate(R.menu.menu, menu)
        setOnMenuItemClickListener {
            viewModel.onMenuItemClicked(requireContext(), it.itemId)
            true
        }
    }

    private fun setupTelegramButton() {
        with(binding.includeMainLoaded.includeMainLoadedCardBrand) {
            btnOpenTelegram.setOnClickListener {
                viewModel.openTelegram(requireContext())
            }
            btnOpenDevTelegram.setOnClickListener {
                viewModel.openDevTelegram(requireContext())
            }
            btnCheckUpdate.setOnClickListener {
                viewModel.openGitHubReleases(requireContext())
            }
        }
        binding.includeMainError.mainErrorTelegram.setOnClickListener {
            viewModel.openTelegram(requireContext())
        }
    }

    private fun setupDiagnostics() {
        binding.includeMainLoaded.includeMainLoadedCardDiag.btnDiagRestartTelephony.setOnClickListener {
            viewModel.onRestartTelephonyClicked()
        }
        binding.includeMainError.mainErrorRestartTelephony.setOnClickListener {
            viewModel.onRestartTelephonyClicked()
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.isRooted.collect { rooted ->
                binding.includeMainLoaded.includeMainLoadedCardDiag.diagStatusRoot.text =
                    getString(if (rooted) R.string.status_root_ok else R.string.status_root_fail)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.isHookConnected.collect { hook ->
                binding.includeMainLoaded.includeMainLoadedCardDiag.diagStatusHook.text =
                    getString(if (hook) R.string.status_hook_ok else R.string.status_hook_fail)
            }
        }
    }

    private fun setupSlotSelector() {
        val card = binding.includeMainLoaded.includeMainLoadedCardEdit
        card.btnSlot1.setOnClickListener {
            viewModel.onSlotSelected(0)
            updateSlotButtonStyles(0)
        }
        card.btnSlot2.setOnClickListener {
            viewModel.onSlotSelected(1)
            updateSlotButtonStyles(1)
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.selectedSlot.collect { slot ->
                updateSlotButtonStyles(slot)
            }
        }
    }

    private fun updateSlotButtonStyles(slot: Int) = with(binding.includeMainLoaded.includeMainLoadedCardEdit) {
        val activeColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.neon_cyan)
        val inactiveColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_secondary)
        val strokeActive = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.neon_cyan)
        val strokeInactive = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.card_stroke)

        if (slot == 0) {
            btnSlot1.strokeWidth = 4
            btnSlot1.strokeColor = android.content.res.ColorStateList.valueOf(strokeActive)
            btnSlot1.setTextColor(activeColor)
            btnSlot2.strokeWidth = 2
            btnSlot2.strokeColor = android.content.res.ColorStateList.valueOf(strokeInactive)
            btnSlot2.setTextColor(inactiveColor)
        } else {
            btnSlot1.strokeWidth = 2
            btnSlot1.strokeColor = android.content.res.ColorStateList.valueOf(strokeInactive)
            btnSlot1.setTextColor(inactiveColor)
            btnSlot2.strokeWidth = 4
            btnSlot2.strokeColor = android.content.res.ColorStateList.valueOf(strokeActive)
            btnSlot2.setTextColor(activeColor)
        }
    }

    private fun setupSaveButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainLoaded.includeMainLoadedCardEdit.mainLoadedCardEditInputSave) {
            onClicked().collect {
                it.hideIme()
                viewModel.onSaveClicked()
            }
        }
    }

    private fun setupCloseButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainError.mainErrorClose) {
            onClicked().collect {
                requireActivity().finish()
            }
        }
    }

    private fun setupInput() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        with(binding.includeMainLoaded.includeMainLoadedCardEdit.mainLoadedCardEditEdit) {
            onChanged().collect {
                val text = it?.toString() ?: ""
                viewModel.onNumberChanged(text)
                validateNumber(text)
            }
        }
    }

    private fun validateNumber(text: String) = with(binding.includeMainLoaded.includeMainLoadedCardEdit) {
        if (text.isEmpty()) {
            valFeedbackText.visibility = View.GONE
            return@with
        }
        val isValid = text.matches(Regex("^(\\+)?[0-9]{10,15}$"))
        if (isValid) {
            valFeedbackText.visibility = View.GONE
        } else {
            valFeedbackText.visibility = View.VISIBLE
            valFeedbackText.setText(R.string.val_invalid_format)
        }
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        binding.includeMainLoading.root.isVisible = state is State.Loading
        binding.includeMainLoaded.root.isVisible = state is State.Loaded
        binding.includeMainError.root.isVisible = state is State.Error
        when(state) {
            is State.Loading -> setupWithLoading(state.loadType)
            is State.Error -> setupWithError(state.errorType)
            is State.Loaded -> setupWithLoaded(state.number)
        }
    }

    private fun setupNumber() {
        viewModel.number.value?.let {
            handleNumber(it)
        } ?: run {
            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                viewModel.number.filterNotNull().take(1).collect {
                    handleNumber(it)
                }
            }
        }
    }

    private fun handleNumber(number: String) = with(binding.includeMainLoaded.includeMainLoadedCardEdit) {
        mainLoadedCardEditEdit.setText(number, TextView.BufferType.EDITABLE)
    }

    private fun setupWithLoading(loadType: LoadType) = with(binding.includeMainLoading) {
        mainLoading.setText(loadType.contentRes)
    }

    private fun setupWithError(errorType: ErrorType) = with(binding.includeMainError) {
        mainErrorContent.setText(errorType.messageRes)
    }

    private fun setupWithLoaded(number: String) = with(binding.includeMainLoaded) {
        val formattedNumber = number.ifEmpty {
            getString(R.string.main_loaded_card_edit_number_empty)
        }
        includeMainLoadedCardEdit.mainLoadedCardEditCurrent.text =
            getString(R.string.main_loaded_card_edit_content, formattedNumber)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}
