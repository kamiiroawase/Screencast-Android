package com.github.kamiiroawase.screencast.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.Toast
import com.github.kamiiroawase.screencast.R
import com.github.kamiiroawase.screencast.databinding.ActivityFeedbackBinding

class FeedbackActivity : BaseActivity() {
    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpClickListener()
        setUpEditTextChangedListener()

        setStatusBarWrap(binding.statusBarWrap)
    }

    private fun setUpEditTextChangedListener() {
        binding.editTextFankuineirong.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //
            }

            override fun afterTextChanged(s: Editable) {
                if (!TextUtils.isEmpty(s.toString())) {
                    binding.num.text = s.length.toString()
                }
            }
        })
    }

    private fun setUpClickListener() {
        binding.submit.setOnClickListener {
            if (binding.editTextShoujihao.text.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    R.string.qingshurushoujihao,
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (binding.editTextFankuineirong.text.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    R.string.fankuiyijian3,
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            binding.num.text = "0"
            binding.editTextFankuineirong.setText("")

            Toast.makeText(
                this,
                R.string.tijiaochenggong,
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}
