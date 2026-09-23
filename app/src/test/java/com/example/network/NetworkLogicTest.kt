package com.example.network

import com.example.network.NetworkConstants.areSectionsEqual
import org.junit.Test
import org.junit.Assert.*

class NetworkLogicTest {

    @Test
    fun testSectionNormalizationEquality() {
        // GIVEN: Nombres de secciones con diferentes formatos
        val s1 = "101"
        val s2 = " 101 "
        val s3 = "101"
        val s4 = "6to B"
        val s5 = "6TO b"

        // THEN: Deberían considerarse iguales ignorando espacios y mayúsculas
        assertTrue(areSectionsEqual(s1, s2))
        assertTrue(areSectionsEqual(s1, s3))
        assertTrue(areSectionsEqual(s4, s5))
    }

    @Test
    fun testSectionInequality() {
        // GIVEN: Secciones realmente diferentes
        val s1 = "101"
        val s2 = "102"
        
        // THEN: No deberían ser iguales
        assertFalse(areSectionsEqual(s1, s2))
    }
}
