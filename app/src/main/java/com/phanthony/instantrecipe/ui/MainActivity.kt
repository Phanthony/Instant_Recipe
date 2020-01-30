package com.phanthony.instantrecipe.ui

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.phanthony.instantrecipe.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.phanthony.instantrecipe.main.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var startSnackBar: Snackbar
    lateinit var finishSnackBar: Snackbar
    lateinit var viewModel: RecipeViewModel

    val PERMISSIONS = arrayOf(
        android.Manifest.permission.WRITE_CONTACTS,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(!hasPermissions(this,*PERMISSIONS)){
            ActivityCompat.requestPermissions(this,PERMISSIONS,1)
        }

        viewModel = ViewModelProviders.of(this,RecipeViewModelFactory(this.application)).get(RecipeViewModel::class.java)

        viewModel.observeQueue(this)

        val host: NavHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment? ?: return
        val navController = host.navController
        setupBottomNav(navController)

        startSnackBar = Snackbar.make(findViewById(R.id.mainLayout),getString(R.string.scan_start),Snackbar.LENGTH_SHORT).setAnchorView(bottomNav)
        finishSnackBar = Snackbar.make(findViewById(R.id.mainLayout),getString(R.string.scan_finish),Snackbar.LENGTH_SHORT).setAnchorView(bottomNav)

        observeScanner()

    }

    fun observeScanner(){
        viewModel.scanning.observe(this, Observer { status ->
            when(status){
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

    private fun setupBottomNav(nav: NavController){
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.itemIconTintList = null
        bottomNav.background = ColorDrawable(getColor(R.color.backgroundColor))
        bottomNav.setOnNavigationItemSelectedListener { item ->
            nav.navigate(item.itemId)
            true
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }




}
