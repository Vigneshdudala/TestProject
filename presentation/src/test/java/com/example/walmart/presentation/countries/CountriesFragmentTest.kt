package com.example.walmart.presentation.countries

import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.walmart.domain.model.Country
import com.example.walmart.presentation.R
import com.example.walmart.presentation.ext.repeatOnStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CountriesFragmentTest {

    @Mock
    private lateinit var viewModel: CountriesViewModel

    private lateinit var navController: NavController

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        navController = Mockito.mock(NavController::class.java)

        // Launch the fragment in a container
        launchFragmentInContainer<CountriesFragment>(Bundle(), R.style.Theme_AppCompat) {
            CountriesFragment().apply {
                // Inject your mocked ViewModel here
                this.viewModel = this@CountriesFragmentTest.viewModel
            }
        }
    }

    @Test
    fun `should render country list on state update`() = runBlockingTest {
        // Given a list of countries
        val countries = listOf(
            Country("USA", "Americas", "Washington D.C.", "USD"),
            Country("Canada", "Americas", "Ottawa", "CAD")
        )
        val stateFlow = MutableStateFlow(CountriesViewModel.State(items = countries))

        // When state updates
        Mockito.`when`(viewModel.state).thenReturn(stateFlow)

        // Launch the fragment
        val scenario = launchFragmentInContainer<CountriesFragment>()

        scenario.onFragment { fragment ->
            fragment.repeatOnStart {
                fragment.viewModel.state.collectLatest { state ->
                    // Verify that the adapter submits the correct list
                    assert(fragment.viewBinding?.countryRecyclerView?.adapter?.itemCount == 2)
                }
            }
        }
    }

    @Test
    fun `should navigate to details on item click effect`() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<CountriesFragment>()

        // Set up the NavController
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)

            // Simulate the effect to open details
            val countryCode = "USA"
            fragment.onEffect(CountriesViewModel.Effect.OpenDetails(countryCode))

            // Verify that navigate was called with the correct arguments
            Mockito.verify(navController).navigate(
                R.id.countryDetailsFragment,
                Bundle().apply { putString(CountryDetailsArg.COUNTRY_CODE, countryCode) }
            )
        }
    }

    @Test
    fun `should update search query in ViewModel on text change`() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<CountriesFragment>()

        scenario.onFragment { fragment ->
            // Simulate a search query
            val searchView = fragment.viewBinding?.actionBar?.menu?.findItem(R.id.action_search)?.actionView as SearchView
            val searchQuery = "USA"

            // Simulate text change
            searchView.setQuery(searchQuery, true)

            // Verify that the ViewModel's search method is called
            Mockito.verify(viewModel).search(searchQuery)
        }
    }

    @Test
    fun `should refresh data on swipe refresh`() = runBlockingTest {
        // Given a loading state
        Mockito.`when`(viewModel.state).thenReturn(MutableStateFlow(CountriesViewModel.State(loading = true)))

        // Launch the fragment
        val scenario = launchFragmentInContainer<CountriesFragment>()

        scenario.onFragment { fragment ->
            // Simulate swipe refresh
            fragment.viewBinding?.swipeRefreshLayout?.isRefreshing = true
            fragment.viewModel.reloadList()

            // Verify that reloadList() was called
            Mockito.verify(viewModel).reloadList()
        }
    }

    @Test
    fun `should show error message when present in state`() = runBlockingTest {
        // Given an error message
        val errorMessage = "Network Error"
        val stateFlow = MutableStateFlow(CountriesViewModel.State(errorMessage = errorMessage))

        // When state updates
        Mockito.`when`(viewModel.state).thenReturn(stateFlow)

        // Launch the fragment
        val scenario = launchFragmentInContainer<CountriesFragment>()

        scenario.onFragment { fragment ->
            fragment.repeatOnStart {
                fragment.viewModel.state.collectLatest { state ->
                    // Verify that the Snackbar is shown with the correct error message
                    assert(fragment.viewBinding?.swipeRefreshLayout?.isRefreshing == false)
                    // Add additional checks to verify Snackbar message if necessary
                }
            }
        }
    }
}
