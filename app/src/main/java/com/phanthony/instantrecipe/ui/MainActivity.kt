package com.phanthony.instantrecipe.ui

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.phanthony.instantrecipe.R
import com.phanthony.instantrecipe.database.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.phanthony.instantrecipe.main.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var startSnackBar: Snackbar
    lateinit var finishSnackBar: Snackbar
    lateinit var viewModel: RecipeViewModel
    lateinit var navController: NavController
    lateinit var settingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, RecipeViewModelFactory(this.application))
            .get(RecipeViewModel::class.java)

        viewModel.observeQueue(this)
        viewModel.observeIngList(this)
        viewModel.observeSettings(this)

        val host: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment?
                ?: return
        navController = host.navController
        setupBottomNav(navController)

        startSnackBar = Snackbar.make(
            findViewById(R.id.mainLayout),
            getString(R.string.scan_start),
            Snackbar.LENGTH_SHORT
        ).setAnchorView(bottomNav)
        finishSnackBar = Snackbar.make(
            findViewById(R.id.mainLayout),
            getString(R.string.scan_finish),
            Snackbar.LENGTH_SHORT
        ).setAnchorView(bottomNav)

        setupSettingDialog()

        observeScanner()
    }

    fun setupSettingDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.settings_dialog, null)
        val rg = view.findViewById<RadioGroup>(R.id.searchRadioGroup)
        rg.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.maxIngredientsRadio -> {
                    viewModel.changeSearchSettings(1)
                }
                R.id.minIngredientsRadio -> {
                    viewModel.changeSearchSettings(2)
                }
            }
        }
        dialogBuilder.setView(view)
        dialogBuilder.setTitle(getString(R.string.settings))
        settingDialog = dialogBuilder.create()
    }

    fun observeScanner() {
        viewModel.scanning.observe(this, Observer { status ->
            when (status) {
                SCANNING -> {
                    startSnackBar.show()
                }
                FINISHED -> {
                    finishSnackBar.show()
                    viewModel.setScanning(IDLE)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = MenuInflater(baseContext)
        inflater.inflate(R.menu.option_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.findRecipeFragment -> {
                navController.navigate(R.id.findRecipeFragment)
                true
            }
            R.id.settingsMenu -> {
                settingDialog.show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun setupBottomNav(nav: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.itemIconTintList = null
        bottomNav.background = ColorDrawable(getColor(R.color.backgroundColor))
        bottomNav.setOnNavigationItemSelectedListener { item ->
            nav.navigate(item.itemId)
            true
        }
    }
}
