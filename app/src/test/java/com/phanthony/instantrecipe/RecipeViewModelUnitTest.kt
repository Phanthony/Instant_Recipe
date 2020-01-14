package com.phanthony.instantrecipe

import android.app.Application
import com.phanthony.instantrecipe.database.RecipeDataBase
import com.phanthony.instantrecipe.main.RecipeViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import com.phanthony.instantrecipe.service.SpoonacularService
import com.google.common.truth.Truth.assertThat
import org.junit.Before


@RunWith(MockitoJUnitRunner::class)
class RecipeViewModelUnitTest {

    @Mock
    lateinit var service: SpoonacularService
    @Mock
    lateinit var db: RecipeDataBase
    @Mock
    lateinit var app: Application
    lateinit var viewModel: RecipeViewModel

    @Before
    fun setup(){
        viewModel = RecipeViewModel(app,db,service)
    }

    @Test
    fun `init ViewModel`(){
        val test = RecipeViewModel(app,db,service)
    }

    @Test
    fun `setUpSet test1`(){
        val testSet = mutableSetOf("Apple","Berry","Eggs")
        val testResult = viewModel.setUpSet(testSet)
        val expected = "Apple,+Berry,+Eggs"
        assertThat(testResult).isEqualTo(expected)
    }

    @Test
    fun `setUpSet test2`(){
        val testSet = mutableSetOf("Apples")
        val testResult = viewModel.setUpSet(testSet)
        val expected = "Apples"
        assertThat(testResult).isEqualTo(expected)
    }

    @Test
    fun `setUpSet test3`(){
        val testSet = mutableSetOf("Apples","Cranberry Juice","Whole Milk")
        val testResult = viewModel.setUpSet(testSet)
        val expected = "Apples,+Cranberry+Juice,+Whole+Milk"
        assertThat(testResult).isEqualTo(expected)
    }

    @Test
    fun `setUpSet test4`(){
        val testSet = mutableSetOf("Apple Pie")
        val testResult = viewModel.setUpSet(testSet)
        val expected = "Apple+Pie"
        assertThat(testResult).isEqualTo(expected)
    }

    @Test
    fun `setUpSet test5`(){
        val testSet = mutableSetOf<String>()
        val testResult = viewModel.setUpSet(testSet)
        val expected = ""
        assertThat(testResult).isEqualTo(expected)
    }
}