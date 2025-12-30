package com.github.kamiiroawase.screencast.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.github.kamiiroawase.screencast.R
import com.github.kamiiroawase.screencast.databinding.ActivityMainBinding
import com.github.kamiiroawase.screencast.fragment.WodeFragment
import com.github.kamiiroawase.screencast.fragment.LuzhiFragment

class MainActivity : BaseActivity() {
    private val currentItemIdName = "CURRENT_ITEM_ID"

    private var currentItemId = R.id.navigationLuzhi

    private lateinit var binding: ActivityMainBinding

    private lateinit var fragments: Map<Int, Fragment>

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(currentItemIdName, currentItemId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            restoreFragments(savedInstanceState)
        } else {
            setUpFragments()
        }

        setUpItemSelectedListener()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    moveTaskToBack(true)
                }
            }
        )
    }

    private fun setUpItemSelectedListener() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            switchFragment(item.itemId)
            true
        }
    }

    private fun setUpFragments() {
        fragments = mapOf(
            R.id.navigationLuzhi to LuzhiFragment(),
            R.id.navigationWode to WodeFragment()
        )

        val transaction = supportFragmentManager.beginTransaction()

        fragments.forEach { (key: Int, value: Fragment) ->
            transaction.add(R.id.navHostFragment, value, key.toString())

            if (key != R.id.navigationLuzhi) {
                transaction.hide(value)
            }
        }

        transaction.commitNowAllowingStateLoss()
    }

    private fun restoreFragments(savedInstanceState: Bundle) {
        currentItemId = savedInstanceState.getInt(currentItemIdName, R.id.navigationLuzhi)

        val luzhiFragment = supportFragmentManager
            .findFragmentByTag(R.id.navigationLuzhi.toString())
                as? LuzhiFragment
            ?: LuzhiFragment()
        val wodeFragment = supportFragmentManager
            .findFragmentByTag(R.id.navigationWode.toString())
                as? WodeFragment
            ?: WodeFragment()

        fragments = mapOf(
            R.id.navigationLuzhi to luzhiFragment,
            R.id.navigationWode to wodeFragment
        )
    }

    private fun switchFragment(itemId: Int) {
        if (itemId == currentItemId) return

        val showFragment = fragments[itemId] ?: return
        val hideFragment = fragments[currentItemId]!!

        val transaction = supportFragmentManager.beginTransaction()

        transaction.hide(hideFragment)
        transaction.show(showFragment)

        transaction.commitAllowingStateLoss()

        currentItemId = itemId
    }
}
