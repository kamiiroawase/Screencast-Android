package com.github.kamiiroawase.screencast.activity

import android.os.Bundle
import com.github.kamiiroawase.screencast.databinding.ActivityWebBinding

class WebActivity : BaseActivity() {
    private lateinit var binding: ActivityWebBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpRefreshListener()

        setUpWebview()

        setStatusBarWrap(binding.statusBarWrap)
    }

    private fun setUpWebview() {
        val url = intent.getStringExtra("url") ?: "https://www.irs.gov/"
        binding.webView.loadUrl(url)
    }

    private fun setUpRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.webView.loadUrl(binding.webView.url.toString())
            binding.webView.postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false
            }, 1000)
        }
    }
}
