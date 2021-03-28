package com.mako

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mako.mlrecognition.feature.MainViewModel
import com.mako.scansdk.ScanSDK
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    private lateinit var viewModel: MainViewModel

    @Mock
    private lateinit var scanSDK: ScanSDK

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        initViewModel()
    }

    @Test
    fun `navigate to ScanFaceFragment if Scan face clicked`(){
        runBlocking {
            initViewModel()
            viewModel.onStartFaceScanClicked()
            MatcherAssert.assertThat(
                viewModel.navigation.value,
                CoreMatchers.instanceOf(MainViewModel.Navigation.FaceRecognition::class.java)
            )
        }
    }

    @Test
    fun `navigate to ScanTextFragment if Scan face clicked`(){
        runBlocking {
            initViewModel()
            viewModel.onStartTextScanClicked()
            MatcherAssert.assertThat(
                viewModel.navigation.value,
                CoreMatchers.instanceOf(MainViewModel.Navigation.TextRecognition::class.java)
            )
        }
    }

    @Test
    fun `show image if face recognition returned a path`(){
        runBlocking {
            initViewModel()
            viewModel.onHandleFaceScanResult("/sdcard/path/image.png")
            MatcherAssert.assertThat(
                viewModel.image.value,
                CoreMatchers.equalTo("/sdcard/path/image.png")
            )
        }
    }

    @Test
    fun `show error message if face recognition returned null`(){
        runBlocking {
            initViewModel()
            viewModel.onHandleFaceScanResult(null)
            MatcherAssert.assertThat(
                viewModel.text.value,
                CoreMatchers.instanceOf(MainViewModel.TextState.Error::class.java)
            )
        }
    }

    @Test
    fun `show error message if text recognition returned null`(){
        runBlocking {
            initViewModel()
            viewModel.onHandleTextScanResult(null)
            MatcherAssert.assertThat(
                viewModel.text.value,
                CoreMatchers.instanceOf(MainViewModel.TextState.Error::class.java)
            )
        }
    }

    @Test
    fun `show scanned text if text recognition returned a value`(){
        runBlocking {
            initViewModel()
            viewModel.onHandleTextScanResult("blah-blah")
            MatcherAssert.assertThat(
                viewModel.text.value,
                CoreMatchers.instanceOf(MainViewModel.TextState.Message::class.java)
            )
        }
    }


    @Test
    fun `call SDK clean methos`(){
        runBlocking {
            initViewModel()
            viewModel.onDestroy()
            verify(scanSDK, Mockito.times(1)).clearCachedData()
        }
    }

    @After
    fun tearDown() {
    }

    private fun initViewModel() {
        viewModel = MainViewModel(scanSDK)
    }
}