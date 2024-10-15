package com.example.walmart.presentation.countries

import com.example.walmart.domain.error.ErrorFormatter
import com.example.walmart.domain.model.Country
import com.example.walmart.domain.provider.DispatcherProvider
import com.example.walmart.domain.repo.CountryRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
class CountriesViewModelTest {

    private val countryRepo: CountryRepo = mock()
    private val dispatcherProvider: DispatcherProvider = mock()
    private val errorFormatter: ErrorFormatter = mock()

    private lateinit var viewModel: CountriesViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CountriesViewModel(countryRepo, dispatcherProvider, errorFormatter)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain() // Reset the main dispatcher
        testDispatcher.cleanupTestCoroutines() // Clean up coroutines
    }

    @Test
    fun `should load countries and update state on initialization`() = runTest {
        val countries = listOf(Country("USA", "Americas"), Country("Canada", "Americas"))
        whenever(countryRepo.getCountries()).thenReturn(countries)

        viewModel.state.collect { state ->
            assert(state.loading) // Initially loading should be true
        }

        // Execute the loading
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.collect { state ->
            assert(!state.loading) // After loading, should be false
            assert(state.items == countries) // Should have loaded countries
            assert(state.originalItems == countries) // Should store original items
        }
    }

    @Test
    fun `should update state when searching for a country`() = runTest {
        val countries = listOf(Country("USA", "Americas"), Country("Canada", "Americas"))
        whenever(countryRepo.getCountries()).thenReturn(countries)
        
        // Load items first
        viewModel.reloadList()
        testDispatcher.scheduler.advanceUntilIdle() // Execute loading

        // Now perform the search
        viewModel.search("USA")

        // Allow debounce time to pass
        testDispatcher.scheduler.advanceTimeBy(201)

        viewModel.state.collect { state ->
            assert(state.items == listOf(Country("USA", "Americas"))) // Should filter to USA
        }
    }

    @Test
    fun `should handle error when loading countries`() = runTest {
        val error = RuntimeException("Network Error")
        whenever(countryRepo.getCountries()).thenThrow(error)
        whenever(errorFormatter.getDisplayErrorMessage(error)).thenReturn("Error message")

        viewModel.reloadList()

        // Execute the loading
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.collect { state ->
            assert(!state.loading) // Should not be loading
            assert(state.errorMessage == "Error message") // Should display error message
        }
    }

    @Test
    fun `should send effect when an item is clicked`() = runTest {
        val country = Country("USA", "Americas")
        val effectChannel = viewModel.effectFlow

        // Perform item click
        viewModel.onItemClick(country)

        // Collect the effects emitted
        effectChannel.collect { effect ->
            assert(effect is CountriesViewModel.Effect.OpenDetails) // Check that the effect is of type OpenDetails
            assert((effect as CountriesViewModel.Effect.OpenDetails).countryCode == "USA") // Check country code
        }
    }
}
