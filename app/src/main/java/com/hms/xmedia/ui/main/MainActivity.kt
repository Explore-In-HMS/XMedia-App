/*
 * Copyright 2022. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hms.xmedia.ui.main

import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.hms.xmedia.R
import com.hms.xmedia.base.BaseActivity
import com.hms.xmedia.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun getActivityViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun setup() {
        super.setup()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment

        navController = navHostFragment.navController

        activityViewBinding.bottomNavigationView.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment,
                R.id.homeFragment,
                R.id.projectsFragment,
                R.id.settingsFragment
            )
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            checkBottomNavigationStatus(destination)
        }
    }

    private fun checkBottomNavigationStatus(destination: NavDestination) {
        when (destination.id) {
            R.id.homeFragment -> {
                showBottomNavigationView(true)
            }
            R.id.projectsFragment -> {
                showBottomNavigationView(true)
            }
            R.id.settingsFragment -> {
                showBottomNavigationView(true)
            }
            else -> {
                showBottomNavigationView(false)
            }
        }
    }

    private fun showBottomNavigationView(value: Boolean) {
        if (value) {
            activityViewBinding.bottomNavigationView.visibility = View.VISIBLE
        } else {
            activityViewBinding.bottomNavigationView.visibility = View.GONE
        }
    }


}