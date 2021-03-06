package com.phanthony.instantrecipe

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testRegex(){
        val r = Regex("\\beggs")
        val string = "free range eggs"
        val res = string.contains(r)
        assertTrue(res)
    }

    @Test
    fun testRegex2(){
        val r = Regex("banana")
        val string = "bananas"
        val res = string.contains(r)
        assertTrue(res)
    }
}
