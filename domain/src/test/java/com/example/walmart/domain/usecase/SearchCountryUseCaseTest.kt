package com.example.walmart.domain.usecase

import com.example.walmart.domain.model.Country
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class SearchCountryUseCaseTest {

    private val searchCountryUseCase = SearchCountryUseCase()

    companion object {
        @JvmStatic
        fun searchCountryCases() = listOf(
            // Input list, query, expected output
            TestCase(emptyList(), "", emptyList()),
            TestCase(listOf(Country("USA", "Americas"), Country("Canada", "Americas")), "", listOf(Country("USA", "Americas"), Country("Canada", "Americas"))),
            TestCase(listOf(Country("USA", "Americas"), Country("Canada", "Americas")), "usa", listOf(Country("USA", "Americas"))),
            TestCase(listOf(Country("USA", "Americas"), Country("Mexico", "Americas")), "americas", listOf(Country("USA", "Americas"), Country("Mexico", "Americas"))),
            TestCase(listOf(Country("USA", "Americas"), Country("Canada", "North America")), "north", listOf(Country("Canada", "North America"))),
            TestCase(listOf(Country("Brazil", "South America"), Country("Argentina", "South America")), "Europe", emptyList())
        )

        data class TestCase(val countries: List<Country>, val query: String, val expected: List<Country>)
    }

    @ParameterizedTest
    @MethodSource("searchCountryCases")
    fun `should return correct countries based on query`(testCase: TestCase) {
        val result = searchCountryUseCase(testCase.countries, testCase.query)
        assertEquals(testCase.expected, result)
    }
}
