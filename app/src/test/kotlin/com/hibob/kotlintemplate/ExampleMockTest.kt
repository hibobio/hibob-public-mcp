package com.hibob.kotlintemplate

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.mock

interface Foo {
    fun getName(): String?
}

private const val MOCK_VALUE = "bar"

internal class ExampleMockTest {
    @Test
    fun testMock() {
        val foo = mock<Foo> {}
        Mockito.`when`(foo.getName()).thenReturn(MOCK_VALUE)
        assertThat(foo.getName(), Matchers.equalTo(MOCK_VALUE))
    }
}
