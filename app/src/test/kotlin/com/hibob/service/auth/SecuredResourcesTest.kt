package com.hibob.service.auth

import com.hibob.authentication.Secured
import com.hibob.service.version.VersionController
import jakarta.ws.rs.*
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Test
import org.reflections.Reflections
import org.reflections.scanners.Scanners

class SecuredResourcesTest {
    private val whitelist: List<Class<*>> = listOf(VersionController::class.java)

    @Test
    fun `verify all resources defined Secured annotation`() {
        val reflections =
            Reflections(
                "com.hibob",
                Scanners.MethodsAnnotated,
                Scanners.TypesAnnotated,
                Scanners.SubTypes,
            )
        val methodsWithPathAnnotation =
            reflections.getMethodsAnnotatedWith(Path::class.java).toMutableSet() +
                reflections.getMethodsAnnotatedWith(GET::class.java).toMutableSet() +
                reflections.getMethodsAnnotatedWith(POST::class.java).toMutableSet() +
                reflections.getMethodsAnnotatedWith(PUT::class.java).toMutableSet() +
                reflections.getMethodsAnnotatedWith(DELETE::class.java).toMutableSet()

        val unAnnotatedMethods =
            methodsWithPathAnnotation
                .filter {
                    val methodAnnotation = it.getAnnotation(Secured::class.java)
                    val classAnnotation = it.declaringClass.getAnnotation(Secured::class.java)
                    methodAnnotation == null && classAnnotation == null && !whitelist.contains(it.declaringClass)
                }
        MatcherAssert.assertThat(
            "The following methods are missing @Secured annotation: ${unAnnotatedMethods.sortedBy { it.declaringClass.name }.joinToString(
                ",\n",
            )}",
            unAnnotatedMethods.isEmpty(),
        )
    }
}
