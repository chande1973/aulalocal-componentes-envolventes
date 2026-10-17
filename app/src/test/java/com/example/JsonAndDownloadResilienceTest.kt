package com.example

import com.example.util.closeQuietly
import com.example.util.runSafely
import com.example.util.toSafeJSONArray
import com.example.util.toSafeJSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JsonAndDownloadResilienceTest {

    @Test
    fun testToSafeJSONObject_valid() {
        val jsonStr = "{\"key\":\"value\"}"
        val jsonObj = jsonStr.toSafeJSONObject()
        assertEquals("value", jsonObj.optString("key"))
    }

    @Test
    fun testToSafeJSONObject_invalidOrNull() {
        assertNotNull("Null string should return empty JSONObject", (null as String?).toSafeJSONObject())
        assertEquals(0, (null as String?).toSafeJSONObject().length())

        assertNotNull("Blank string should return empty JSONObject", "   ".toSafeJSONObject())
        assertEquals(0, "   ".toSafeJSONObject().length())

        assertNotNull("Malformed JSON should return empty JSONObject", "invalid_json".toSafeJSONObject())
        assertEquals(0, "invalid_json".toSafeJSONObject().length())
    }

    @Test
    fun testToSafeJSONArray_valid() {
        val jsonArrayStr = "[\"item1\", \"item2\"]"
        val jsonArray = jsonArrayStr.toSafeJSONArray()
        assertEquals(2, jsonArray.length())
        assertEquals("item1", jsonArray.optString(0))
    }

    @Test
    fun testToSafeJSONArray_invalidOrNull() {
        assertNotNull((null as String?).toSafeJSONArray())
        assertEquals(0, (null as String?).toSafeJSONArray().length())

        assertNotNull("".toSafeJSONArray())
        assertEquals(0, "".toSafeJSONArray().length())

        assertNotNull("bad_array".toSafeJSONArray())
        assertEquals(0, "bad_array".toSafeJSONArray().length())
    }

    @Test
    fun testRunSafely() {
        val successResult = runSafely(0) {
            "10".toInt()
        }
        assertEquals(10, successResult)

        val fallbackResult = runSafely(-1) {
            "not_an_int".toInt()
        }
        assertEquals(-1, fallbackResult)
    }

    @Test
    fun testCloseQuietly() {
        var closed = false
        val mockResource = AutoCloseable { closed = true }
        closeQuietly(mockResource)
        assertTrue(closed)

        // Should not throw on null
        closeQuietly(null)
    }
}
