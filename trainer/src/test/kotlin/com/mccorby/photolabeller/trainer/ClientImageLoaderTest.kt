package com.mccorby.photolabeller.trainer

import com.mccorby.photolabeller.repository.LocalDataSource
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.File

internal class ClientImageLoaderTest {

    @Mock
    private lateinit var localDataSource: LocalDataSource
    @Mock
    private lateinit var imageProcessor: ImageProcessor

    private lateinit var cut: ClientImageLoader

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
//        val labels = listOf("airplane", "automobile", "bird", "cat", "deer", "dog", "frog", "horse", "ship", "truck") // cifar10
//        val labels = listOf("cloudy", "rain", "shine", "sunrise") // multiclass-weather-dataset
//        val labels = listOf("dew", "fogsmog", "frost", "glaze", "hail", "lightning", "rain", "rainbow", "rime", "sandstorm", "snow") //
        val labels = listOf("cloudy", "foggy", "rainy", "snowy", "sunny") //
        cut = ClientImageLoader(localDataSource, imageProcessor, labels)
    }

    @Test
    fun `Given a map of labels and files when cut is initialised it has a list of FileData objects`() {
        // Given
        val filesMap = mapOf(
                "cat" to listOf(File("file1"), File("file2")),
                "dog" to listOf(File("file3"), File("file4")))

        whenever(localDataSource.loadTrainingFiles()).thenReturn(filesMap)

        // When/Then

    }

}