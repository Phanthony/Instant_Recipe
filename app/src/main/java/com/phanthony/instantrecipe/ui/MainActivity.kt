package com.phanthony.instantrecipe.ui

import main.FINISHED
import main.IDLE
import main.RecipeViewModel
import main.SCANNING
import android.content.Context
import android.content.pm.PackageManager
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
import com.phanthony.instantrecipe.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var startToast: Toast
    lateinit var finishToast: Toast
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

        viewModel = ViewModelProviders.of(this)[RecipeViewModel::class.java]

        val ingredientMap = hashMapOf<String,String>()

        //get ingredients from ingredients.csv
        CoroutineScope(Dispatchers.IO).launch{
            val ingredients = resources.openRawResource(R.raw.test)
            csvReader().open(ingredients) {
                readAllAsSequence().forEach {
                    val split = it.first().split(";")
                    ingredientMap[split.first()] = split.last()
                }
            }

            viewModel.setMap(ingredientMap)
        }

        viewModel.observeQueue(this)

        val host: NavHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment? ?: return
        val navController = host.navController
        setupBottomNav(navController)

        startToast = Toast.makeText(this, getString(R.string.scan_start), Toast.LENGTH_LONG)
        finishToast = Toast.makeText(this, getString(R.string.scan_finish), Toast.LENGTH_SHORT)

        observeScanner()

    }

    fun observeScanner(){
        viewModel.scanning.observe(this, Observer { status ->
            when(status){
                SCANNING -> {
                    finishToast.cancel()
                    startToast.show()
                }
                FINISHED -> {
                    startToast.cancel()
                    finishToast.show()
                    viewModel.setScanning(IDLE)
                }
            }
        })
    }

    private fun setupBottomNav(nav: NavController){
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.itemIconTintList = null
        bottomNav.setOnNavigationItemSelectedListener { item ->
            nav.navigate(item.itemId)
            true
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }




}
