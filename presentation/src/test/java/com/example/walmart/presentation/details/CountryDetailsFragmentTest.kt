package com.example.walmart.presentation.details

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
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
class CountryDetailsFragmentTest {

    @Mock
    private lateinit var viewModel: CountryDetailsViewModel

    private lateinit var navController: NavController

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        navController = Mockito.mock(NavController::class.java)

        // Launch the fragment in a container
        val fragmentArgs = Bundle().apply {
            // Pass any necessary arguments here
        }
        launchFragmentInContainer<CountryDetailsFragment>(fragmentArgs, R.style.Theme_AppCompat) {
            CountryDetailsFragment().apply {
                // Inject your mocked ViewModel here
                this.viewModel = this@CountryDetailsFragmentTest.viewModel
            }
        }
    }

    @Test
    fun `should render country details on state update`() = runBlockingTest {
        // Given a country state
        val country = Country("USA", "Americas", "Washington D.C.", "USD")
        val stateFlow = MutableStateFlow(CountryDetailsViewModel.State(country = country))

        // When state updates
        Mockito.`when`(viewModel.state).thenReturn(stateFlow)

        // Launch the fragment
        val scenario = launchFragmentInContainer<CountryDetailsFragment>()

        // Collect the UI state
        scenario.onFragment { fragment ->
            fragment.repeatOnStart {
                fragment.viewModel.state.collectLatest { state ->
                    // Verify UI updates
                    assert(fragment.viewBinding?.countryItemLayout?.nameWithRegionView?.text == "USA, Americas")
                    assert(fragment.viewBinding?.countryItemLayout?.codeView?.text == "USD")
                    assert(fragment.viewBinding?.countryItemLayout?.capitalView?.text == "Washington D.C.")
                }
            }
        }
    }

    @Test
    fun `should navigate back on effect OnBack`() {
        // Launch the fragment
        val scenario = launchFragmentInContainer<CountryDetailsFragment>()

        // Set up the NavController
        scenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), navController)

            // Simulate the effect to navigate back
            fragment.onEffect(CountryDetailsViewModel.Effect.OnBack)

            // Verify that popBackStack was called
            Mockito.verify(navController).popBackStack()
        }
    }

    @Test
    fun `should refresh data on swipe refresh`() = runBlockingTest {
        // Given a loading state
        Mockito.`when`(viewModel.state).thenReturn(MutableStateFlow(CountryDetailsViewModel.State(loading = true)))

        // Launch the fragment
        val scenario = launchFragmentInContainer<CountryDetailsFragment>()

        // Collect the UI state
        scenario.onFragment { fragment ->
            // Simulate swipe refresh
            fragment.viewBinding?.swipeRefreshLayout?.isRefreshing = true
            fragment.viewModel.reloadList()

            // Verify that reloadList() was called
            Mockito.verify(viewModel).reloadList()
        }
    }
}
