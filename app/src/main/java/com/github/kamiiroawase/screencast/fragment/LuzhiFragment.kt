package com.github.kamiiroawase.screencast.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.github.kamiiroawase.screencast.databinding.FragmentLuzhiBinding
import com.github.kamiiroawase.screencast.R
import com.github.kamiiroawase.screencast.service.ScreenRecorderService
import com.github.kamiiroawase.screencast.activity.RegionSelectionActivity
import com.github.kamiiroawase.screencast.preference.AppPreference
import com.github.kamiiroawase.screencast.service.FloaterWindowService

class LuzhiFragment : BaseFragment() {
    private var _binding: FragmentLuzhiBinding? = null
    private val binding get() = _binding!!

    private lateinit var directoryPickerLauncher: ActivityResultLauncher<Intent>

    companion object {
        private var INSTANCE: LuzhiFragment? = null

        fun getInstance(): LuzhiFragment {
            return INSTANCE!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLuzhiBinding.inflate(inflater, container, false)

        INSTANCE = this

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        INSTANCE = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setStatusBarWrap(binding.statusBarWrap)

        updateUiButtonShengyinlaiyuan()
        updateUiButtonLupingfenbianlv()
        updateUiButtonLupingfangxiang()
        updateUiButtonLupingzhenshu()
        updateUiButtonLupinghuazhi()
        updateUiButtonKaishiluzhi()
        updateUiButtonLupingquyu()
        updateUiButtonBaocunmulu()
        updateUiButtonXuanfuqiu()

        setButtonClickListeners()
    }

    @SuppressLint("SetTextI18n")
    fun updateUiButtonShengyinlaiyuan() {
        binding.buttonShengyinlaiyuanStatusText.text =
            when (AppPreference.getInstance().getSettingsAudioRecordSwitch()) {
                "1" -> getString(R.string.xitongneishengyin)
                "2" -> getString(R.string.maikefengshengyin)
                else -> getString(R.string.wushengyin)
            }
    }

    @SuppressLint("SetTextI18n")
    fun updateUiButtonLupingfenbianlv() {
        val value = AppPreference.getInstance().getSettingsLupingfenbianlv()

        binding.buttonLupingfenbianlvStatusText.text = when (value) {
            "0" -> getString(R.string.moren)
            else -> value + "P"
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateUiButtonLupingfangxiang() {
        binding.buttonLupingfangxiangStatusText.text =
            when (AppPreference.getInstance().getSettingsLupingfangxiang()) {
                "1" -> getString(R.string.shuping)
                "2" -> getString(R.string.hengping)
                else -> getString(R.string.moren)
            }
    }

    @SuppressLint("SetTextI18n")
    fun updateUiButtonLupinghuazhi() {
        binding.buttonLupinghuazhiStatusText.text =
            AppPreference.getInstance().getSettingsLupinghuazhi() + "Mbps"
    }

    @SuppressLint("SetTextI18n")
    fun updateUiButtonLupingzhenshu() {
        binding.buttonLupingzhenshuStatusText.text =
            AppPreference.getInstance().getSettingsLupingzhenshu() + "FPS"
    }

    @SuppressLint("SetTextI18n")
    fun updateUiButtonKaishiluzhi() {
        if (ScreenRecorderService.isRecording) {
            binding.buttonKaishiluzhiText.text = getString(R.string.tingzhiluzhi)
        } else {
            binding.buttonKaishiluzhiText.text = getString(R.string.kaishiluzhi)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateUiButtonLupingquyu() {
        binding.buttonLupingquyuStatusText.text =
            if (AppPreference.getInstance().getSettingsLupingquyu() != null) {
                getString(R.string.yishezhi)
            } else {
                ""
            }
    }

    @SuppressLint("SetTextI18n")
    fun updateUiButtonBaocunmulu() {
        binding.buttonBaocunmuluStatusText.text =
            if (AppPreference.getInstance().getSettingsBaocunmulu() != "") {
                getString(R.string.yishezhi)
            } else {
                ""
            }
    }

    @SuppressLint("SetTextI18n")
    fun updateUiButtonXuanfuqiu() {
        binding.buttonXuanfuqiuStatusText.text =
            if (AppPreference.getInstance().getSettingsXuanfuqiuSwitch() == "1") {
                getString(R.string.luzhishibuyincang)
            } else if (AppPreference.getInstance().getSettingsXuanfuqiuSwitch() == "2") {
                getString(R.string.luzhishiyincang)
            } else {
                getString(R.string.off)
            }

    }

    fun setButtonClickListeners() {
        binding.buttonKaishiluzhi.setOnClickListener {
            if (!ScreenRecorderService.isRecording) {
                screenCapturePreStart({})
            } else {
                stopScreenRecording()
            }
        }

        binding.buttonLupingquyu.setOnClickListener {
            startActivity(Intent(requireActivity(), RegionSelectionActivity::class.java))
        }

        binding.buttonShengyinlaiyuan.setOnClickListener {
            AlertDialog
                .Builder(requireActivity())
                .setTitle(getString(R.string.shengyinlaiyuan))
                .setItems(
                    arrayOf(
                        getString(R.string.wushengyin),
//                        getString(R.string.xitongneishengyin),
                        getString(R.string.maikefengshengyin)
                    )
                )
                { dialog, which ->
                    when (which) {
                        0 -> AppPreference.getInstance().setSettingsAudioRecordSwitch("0")
//                        1 -> AppPreference.getInstance().setSettingsAudioRecordSwitch("1")
                        1 -> AppPreference.getInstance().setSettingsAudioRecordSwitch("2")
                    }

                    updateUiButtonShengyinlaiyuan()
                }
                .setNegativeButton(getString(R.string.quxiao), null)
                .create()
                .show()
        }

        binding.buttonLupingfenbianlv.setOnClickListener {
            AlertDialog
                .Builder(requireActivity())
                .setTitle(getString(R.string.shipinduanbianchangdu))
                .setItems(
                    arrayOf(
                        getString(R.string.moren), "1080P", "720P", "480P", "360P"
                    )
                )
                { dialog, which ->
                    when (which) {
                        0 -> AppPreference.getInstance().setSettingsLupingfenbianlv("0")
                        1 -> AppPreference.getInstance().setSettingsLupingfenbianlv("1080")
                        2 -> AppPreference.getInstance().setSettingsLupingfenbianlv("720")
                        3 -> AppPreference.getInstance().setSettingsLupingfenbianlv("480")
                        4 -> AppPreference.getInstance().setSettingsLupingfenbianlv("360")
                    }

                    updateUiButtonLupingfenbianlv()
                }
                .setNegativeButton(getString(R.string.quxiao), null)
                .create()
                .show()
        }

        binding.buttonLupinghuazhi.setOnClickListener {
            AlertDialog
                .Builder(requireActivity())
                .setTitle(getString(R.string.huazhiyuegaoyuehao))
                .setItems(
                    arrayOf(
                        "50Mbps", "32Mbps", "24Mbps", "16Mbps", "8Mbps", "6Mbps", "4Mbps", "1Mbps"
                    )
                )
                { dialog, which ->
                    when (which) {
                        0 -> AppPreference.getInstance().setSettingsLupinghuazhi("50")
                        1 -> AppPreference.getInstance().setSettingsLupinghuazhi("32")
                        2 -> AppPreference.getInstance().setSettingsLupinghuazhi("24")
                        3 -> AppPreference.getInstance().setSettingsLupinghuazhi("16")
                        4 -> AppPreference.getInstance().setSettingsLupinghuazhi("8")
                        5 -> AppPreference.getInstance().setSettingsLupinghuazhi("6")
                        6 -> AppPreference.getInstance().setSettingsLupinghuazhi("4")
                        7 -> AppPreference.getInstance().setSettingsLupinghuazhi("1")
                    }

                    updateUiButtonLupinghuazhi()
                }
                .setNegativeButton(getString(R.string.quxiao), null)
                .create()
                .show()
        }

        binding.buttonLupingfangxiang.setOnClickListener {
            AlertDialog
                .Builder(requireActivity())
                .setTitle(getString(R.string.shengchengdeshipinfangxiang))
                .setItems(
                    arrayOf(
                        getString(R.string.shuping), getString(R.string.hengping)
                    )
                )
                { dialog, which ->
                    when (which) {
                        0 -> AppPreference.getInstance().setSettingsLupingfangxiang("1")
                        1 -> AppPreference.getInstance().setSettingsLupingfangxiang("2")
                    }

                    updateUiButtonLupingfangxiang()
                }
                .setNegativeButton(getString(R.string.quxiao), null)
                .create()
                .show()
        }

        binding.buttonLupingzhenshu.setOnClickListener {
            AlertDialog
                .Builder(requireActivity())
                .setTitle(getString(R.string.meimiaoluzhidezhenshu))
                .setItems(
                    arrayOf(
                        "120FPS", "60FPS", "50FPS", "30FPS", "25FPS", "24FPS", "15FPS"
                    )
                )
                { dialog, which ->
                    when (which) {
                        0 -> AppPreference.getInstance().setSettingsLupingzhenshu("120")
                        1 -> AppPreference.getInstance().setSettingsLupingzhenshu("60")
                        2 -> AppPreference.getInstance().setSettingsLupingzhenshu("50")
                        3 -> AppPreference.getInstance().setSettingsLupingzhenshu("30")
                        4 -> AppPreference.getInstance().setSettingsLupingzhenshu("25")
                        5 -> AppPreference.getInstance().setSettingsLupingzhenshu("24")
                        6 -> AppPreference.getInstance().setSettingsLupingzhenshu("15")
                    }

                    updateUiButtonLupingzhenshu()
                }
                .setNegativeButton(getString(R.string.quxiao), null)
                .create()
                .show()
        }

        binding.buttonXuanfuqiu.setOnClickListener {
            if (Settings.canDrawOverlays(requireContext())) {
                AlertDialog
                    .Builder(requireActivity())
                    .setTitle(getString(R.string.xuanfuqiu))
                    .setItems(
                        arrayOf(
                            getString(R.string.off),
                            getString(R.string.luzhishibuyincang),
                            getString(R.string.luzhishiyincang)
                        )
                    )
                    { dialog, which ->
                        when (which) {
                            0 -> {
                                FloaterWindowService.isShouldShowing = false

                                AppPreference.getInstance().setSettingsXuanfuqiuSwitch("0")

                                requireContext().startService(
                                    Intent(
                                        requireContext(),
                                        FloaterWindowService::class.java
                                    ).apply {
                                        action = FloaterWindowService.ACTION_STOP_FLOATING
                                    }
                                )
                            }

                            1 -> {
                                FloaterWindowService.isShouldShowing = true

                                requireContext().startService(
                                    Intent(
                                        requireContext(),
                                        FloaterWindowService::class.java
                                    ).apply {
                                        action = FloaterWindowService.ACTION_START_FLOATING
                                    }
                                )

                                AppPreference.getInstance().setSettingsXuanfuqiuSwitch("1")
                            }

                            2 -> {
                                if (!ScreenRecorderService.isRecording) {
                                    FloaterWindowService.isShouldShowing = true

                                    requireContext().startService(
                                        Intent(
                                            requireContext(),
                                            FloaterWindowService::class.java
                                        ).apply {
                                            action = FloaterWindowService.ACTION_START_FLOATING
                                        }
                                    )
                                } else if (FloaterWindowService.isShowing) {
                                    FloaterWindowService.isShouldShowing = false

                                    requireContext().startService(
                                        Intent(
                                            requireContext(),
                                            FloaterWindowService::class.java
                                        ).apply {
                                            action = FloaterWindowService.ACTION_STOP_FLOATING
                                        }
                                    )
                                }

                                AppPreference.getInstance().setSettingsXuanfuqiuSwitch("2")
                            }
                        }

                        updateUiButtonXuanfuqiu()
                    }
                    .setNegativeButton(getString(R.string.quxiao), null)
                    .create()
                    .show()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = "package:${requireContext().packageName}".toUri()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                requireContext().startActivity(intent)
            }
        }

        directoryPickerLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.data?.let { treeUri ->
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION

                    try {
                        requireActivity().contentResolver.takePersistableUriPermission(
                            treeUri,
                            takeFlags
                        )
                        AppPreference.getInstance().setSettingsBaocunmulu(treeUri.toString())
                        updateUiButtonBaocunmulu()
                    } catch (_: Exception) {

                    }
                }
            }
        }

        binding.buttonBaocunmulu.setOnClickListener {
            directoryPickerLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            })
        }
    }

    fun screenCapturePreStart(callback: () -> Unit, permissionCallback: (() -> Unit)? = null) {
        pendingStartCallback = callback
        pendingPermissionCallback = permissionCallback

        val permissionsNeeded = mutableListOf<String>()

        val fn1 = fun(): AppPreference {
            return AppPreference.getInstance()
        }

        val fn2 = fun(permission: String): Boolean {
            return ContextCompat
                .checkSelfPermission(requireContext(), permission) !=
                    PackageManager.PERMISSION_GRANTED
        }

        val boolean1 = fn1().getSettingsAudioRecordSwitch() == "2"
        var boolean2 = fn2(Manifest.permission.RECORD_AUDIO)

        if (boolean1 && boolean2) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            boolean2 = fn2(Manifest.permission.READ_MEDIA_IMAGES)
//
//            if (boolean2) {
//                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
//            }

//            boolean2 = fn2(Manifest.permission.READ_MEDIA_AUDIO)
//
//            if (boolean2) {
//                permissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO)
//            }

            boolean2 = fn2(Manifest.permission.READ_MEDIA_VIDEO)

            if (boolean2) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            boolean2 = fn2(Manifest.permission.READ_EXTERNAL_STORAGE)

            if (boolean2) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            boolean2 = fn2(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (boolean2) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (pendingPermissionCallback == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                boolean2 = fn2(Manifest.permission.POST_NOTIFICATIONS)

                if (boolean2) {
                    permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissions.launch(permissionsNeeded.toTypedArray())
        } else {
            requestScreenCapturePermission()
        }
    }

    private fun requestScreenCapturePermission() {
        val projectionManager = requireContext()
            .getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager
        val captureIntent = projectionManager.createScreenCaptureIntent()
        requestScreenCapture.launch(captureIntent)
    }

    private fun startScreenRecording(data: Intent) {
        var metrics: DisplayMetrics

        val windowManager = requireContext()
            .getSystemService(Context.WINDOW_SERVICE)
                as WindowManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            metrics = resources.displayMetrics

            val bounds = windowManager.currentWindowMetrics.bounds

            metrics.widthPixels = bounds.width()
            metrics.heightPixels = bounds.height()
        } else {
            metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
        }

        val intent = Intent(requireContext(), ScreenRecorderService::class.java).apply {
            action = ScreenRecorderService.ACTION_START_RECORDING
            putExtra(ScreenRecorderService.EXTRA_RESULT_DATA, data)
            putExtra(ScreenRecorderService.EXTRA_SCREEN_WIDTH, metrics.widthPixels)
            putExtra(ScreenRecorderService.EXTRA_SCREEN_HEIGHT, metrics.heightPixels)
            putExtra(ScreenRecorderService.EXTRA_SCREEN_DENSITY, metrics.densityDpi)
        }

        requireContext().startForegroundService(intent)

        pendingStartCallback?.invoke()
    }

    fun stopScreenRecording() {
        val intent = Intent(requireContext(), ScreenRecorderService::class.java).apply {
            action = ScreenRecorderService.ACTION_STOP_RECORDING
        }

        requireContext().startForegroundService(intent)

        myShortToast(getString(R.string.tingzhiluzhi))
    }

    private var pendingStartCallback: (() -> Unit)? = null

    private var pendingPermissionCallback: (() -> Unit)? = null

    private val requestPermissions =
        registerForActivityResult(RequestMultiplePermissions()) { permissions ->
            if (!permissions.all { it.value }) {
                if (pendingPermissionCallback == null) {
                    screenCapturePreStart({}, {
                        myShortToast(getString(R.string.xuyaosuoyouquanxian))
                    })
                }
            } else {
                requestScreenCapturePermission()
            }
        }

    private val requestScreenCapture =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (!(result.resultCode == Activity.RESULT_OK && result.data != null)) {
                myShortToast(getString(R.string.pingmuluzhiquanxianbeijujue))
            } else {
                startScreenRecording(result.data!!)
            }
        }
}
