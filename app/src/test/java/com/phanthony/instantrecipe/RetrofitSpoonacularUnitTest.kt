package com.phanthony.instantrecipe

import com.example.androidtraining.service.error.NetworkConnectionIssueException
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.phanthony.instantrecipe.database.SpoonacularResult
import com.phanthony.instantrecipe.service.ResponseProcessor
import com.phanthony.instantrecipe.service.RetrofitSpoonacular
import com.phanthony.instantrecipe.service.SpoonacularApi
import io.reactivex.Single
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import retrofit2.adapter.rxjava2.Result as RxResult
import kotlin.Result as KtResult

@RunWith(MockitoJUnitRunner::class)
class RetrofitSpoonacularUnitTest {


    @Mock
    lateinit var service: SpoonacularApi
    @Mock
    lateinit var rp: ResponseProcessor
    lateinit var retro: RetrofitSpoonacular

    @Before
    fun setup(){
        retro = RetrofitSpoonacular(service,rp)
    }

    @Test
    fun `retrofit success 1`(){
        val rxResult = RxResult.response(Response.success<List<SpoonacularResult>>(listOf()))
        val result = Single.just(rxResult)
        val rpResult = ResponseProcessor.ProcessedResult(null,"",listOf<SpoonacularResult>())
        whenever(service.getRecipes(any(), any())).thenReturn(result)
        whenever(rp.process(rxResult)).thenReturn(rpResult)
        val expected = KtResult.success<List<SpoonacularResult>>(listOf())
        val runTest = retro.getRecipesService("test").blockingGet()
        assertThat(runTest).isEqualTo(expected)
    }

    @Test
    fun `retrofit failure 1`(){
        val rxResult = RxResult.response(Response.error<List<SpoonacularResult>>(500, ResponseBody.create(null,"")))
        val result = Single.just(rxResult)
        val rpResult = ResponseProcessor.ProcessedResult<List<SpoonacularResult>>(NetworkConnectionIssueException("You have a no internet"),"You have no internet",null)
        whenever(service.getRecipes(any(), any())).thenReturn(result)
        whenever(rp.process(rxResult)).thenReturn(rpResult)
        val expected = KtResult.failure<List<SpoonacularResult>>(NetworkConnectionIssueException("You have a no internet"))
        val runTest = retro.getRecipesService("test").blockingGet()
        assertThat(runTest).isEqualTo(expected)
    }
}