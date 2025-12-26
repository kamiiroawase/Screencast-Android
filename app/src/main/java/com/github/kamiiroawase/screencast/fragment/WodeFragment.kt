package com.github.kamiiroawase.screencast.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.kamiiroawase.screencast.activity.WebActivity
import com.github.kamiiroawase.screencast.activity.FeedbackActivity
import com.github.kamiiroawase.screencast.databinding.FragmentWodeBinding
import com.github.kamiiroawase.screencast.R

class WodeFragment : BaseFragment() {
    private var _binding: FragmentWodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWodeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpClickListener()

        setUpDangqianbanbenUI()

        setStatusBarWrap(binding.statusBarWrap)
    }

    private fun setUpClickListener() {
        binding.buttonYonghuxieyi.setOnClickListener {
            startActivity(Intent(activity, WebActivity::class.java).apply {
                putExtra("url", "https://www.irs.gov/")
            })
        }

        binding.buttonYinsixieyi.setOnClickListener {
            startActivity(Intent(activity, WebActivity::class.java).apply {
                putExtra("url", "https://www.irs.gov/")
            })
        }

        binding.buttonYijianfankui.setOnClickListener {
            startActivity(Intent(activity, FeedbackActivity::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDangqianbanbenUI() {
        binding.buttonDangqianbanbenText.text = getString(R.string.dangqianbanben) +
                " V" + (
                requireActivity()
                    .packageManager
                    .getPackageInfo(requireActivity().packageName, 0)
                    .versionName
                    ?: ""
                )
    }
}
