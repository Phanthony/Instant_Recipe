package com.phanthony.instantrecipe

import android.app.Application
import com.example.androidtraining.service.error.NetworkConnectionIssueException
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.phanthony.instantrecipe.database.RecipeDao
import com.phanthony.instantrecipe.database.RecipeDataBase
import com.phanthony.instantrecipe.database.RecipeInstruction
import com.phanthony.instantrecipe.database.SpoonacularResult
import com.phanthony.instantrecipe.main.RecipeViewModel
import com.phanthony.instantrecipe.service.SpoonacularService
import com.phanthony.instantrecipe.service.WrappedResult
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class RecipeViewModelUnitTest {

    @Mock
    lateinit var service: SpoonacularService
    @Mock
    lateinit var db: RecipeDataBase
    @Mock
    lateinit var app: Application
    @Mock
    lateinit var recipeDao: RecipeDao

    lateinit var viewModel: RecipeViewModel

    @Before
    fun setup(){
        viewModel = RecipeViewModel(app,db,service)
        whenever(db.recipeDao()).thenReturn(recipeDao)
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

    // Hard to test this function since it doesn't wait for the coroutine
    // to finish before the assert call
    @Test
    fun `getRecipeInstruction test id is replaced 1`(){
        val tId = 123
        val instructions = RecipeInstruction(null,1,"Test", listOf())
        val networkList = listOf(instructions)
        whenever(service.getRecipeInstructions(any())).thenReturn(Single.just(WrappedResult(Result.success(networkList))))
        viewModel.getRecipeInstruction(tId).blockingGet()
        assertThat(instructions.recipeId).isEqualTo(tId)
    }

    @Test
    fun `getRecipeInstruction test id is replaced 2`(){
        val tId = 123
        val instruction1 = RecipeInstruction(null,1,"", listOf())
        val instruction2 = RecipeInstruction(5,1,"", listOf())
        val networkList = listOf(instruction1,instruction2)
        whenever(service.getRecipeInstructions(any())).thenReturn(Single.just(WrappedResult(Result.success(networkList))))
        viewModel.getRecipeInstruction(tId).blockingGet()
        assertThat(instruction1.recipeId).isEqualTo(tId)
        assertThat(instruction2.recipeId).isEqualTo(tId)
    }

    @Test
    fun `getRecipeInstruction test empty result`(){
        val networkList = listOf<RecipeInstruction>()
        whenever(service.getRecipeInstructions(any())).thenReturn(Single.just(WrappedResult(Result.success(networkList))))
        val test = viewModel.getRecipeInstruction(1).blockingGet()
        assertThat(test.isSuccess).isTrue()
        assertThat(test.getOrNull()!!).isEqualTo(2)
    }

    @Test
    fun `getRecipeInstruction test success result`(){
        val tId = 123
        val instructions = RecipeInstruction(null,1,"Test", listOf())
        val networkList = listOf(instructions)
        whenever(service.getRecipeInstructions(any())).thenReturn(Single.just(WrappedResult(Result.success(networkList))))
        val test = viewModel.getRecipeInstruction(tId).blockingGet()
        assertThat(test.isSuccess).isTrue()
        assertThat(test.getOrNull()!!).isEqualTo(1)
    }

    @Test
    fun `getRecipe test1`(){
        val set = mutableSetOf("eggs")
        val mresult = Single.just(WrappedResult(Result.failure<List<SpoonacularResult>>(NetworkConnectionIssueException("You have a bad internet connection"))))
        whenever(service.getRecipes(any())).thenReturn(mresult)
        val g = service.getRecipes("eggs")
        val t = viewModel.getRecipes(set)
        val mtemp = t.blockingGet()
    }
}