package com.mccorby.photolabeller.trainer

import com.mccorby.photolabeller.config.SharedConfig
import com.mccorby.photolabeller.model.IterationLogger
import com.mccorby.photolabeller.model.Stats
import com.mccorby.photolabeller.model.Trainer
import com.mccorby.photolabeller.repository.LocalDataSource
import org.deeplearning4j.nn.api.Model
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.transferlearning.TransferLearning
import org.deeplearning4j.optimize.api.IterationListener
import org.deeplearning4j.optimize.api.TrainingListener
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * This is a singleton object. We don't want to create a trainer each time we need it
 * The trainer holds a reference to the model that is the biggest object in the app and the
 * most costly to build
 * The trainer does a transfer learning process during training, taking the images in the directories
 * and using them to update the model
 *
 */

class TrainerImpl: Trainer {

    companion object {
        var instance: TrainerImpl = TrainerImpl()
    }

    lateinit var config: SharedConfig
    lateinit var localDataSource: LocalDataSource
    lateinit var imageProcessor: ImageProcessorImpl
    var iterationLogger: IterationLogger? = null

    private val iterationListener = object : TrainingListener {
        override fun iterationDone(model: Model?, iteration: Int, epoch: Int) {
            iterationLogger?.onIterationDone("Score at iteration $iteration: ${model!!.score()}")
        }

        override fun onGradientCalculation(model: Model?) {
        }

        override fun onBackwardPass(model: Model?) {
        }

        override fun onForwardPass(model: Model?, activations: MutableList<INDArray>?) {
        }

        override fun onForwardPass(model: Model?, activations: MutableMap<String, INDArray>?) {
        }

        override fun onEpochEnd(model: Model?) {
        }

        override fun onEpochStart(model: Model?) {
        }
    }

    private var model: MultiLayerNetwork? = null

    private var samplesUsedInTraining: Int = 0

    override fun loadModel(location: File): Stats {
        // Load model
        model = ModelSerializer.restoreMultiLayerNetwork(location)// TODO modify to load Keras model

        println(model.toString())

        return Stats("Model loaded")
    }

    override fun train(numSamples: Int, epochs: Int): Stats { //epochs 为 2
        model ?: return Stats("Model not ready")
        val imageLoader = ClientCifarLoader(localDataSource, imageProcessor, config.labels) // 读取欲参与此轮训练的图片与标注
        val dataSetIterator = ClientCifarDataSetIterator( // 建立迭代器
                imageLoader,
                config.batchSize, //batch size 为 16
                1,
                config.labels.size,
                numSamples)

        //指定要設置為“特徵提取器”的圖層指定的圖層及其前面的圖層將被“凍結”，且參數保持不變, 此参数需与伺服器端设置一致
        val newModel = TransferLearning.Builder(model)
                .setFeatureExtractor(config.featureLayerIndex)// index layer 设置为3,
                .build()
        model = newModel

        model!!.setListeners(iterationListener)

        for (i in 0 until epochs) {
            println("Epoch=====================$i")
            dataSetIterator.reset()
            model!!.fit(dataSetIterator)
        }
        samplesUsedInTraining = Math.min(imageLoader.totalExamples(), config.maxSamples)
        // Empty listeners
        model!!.listeners = listOf()

        return Stats("Model trained")
    }

    override fun isModelLoaded() = model != null

    override fun predict(file: File): Stats {
        return model?.let {
            val image = imageProcessor.processImage(file)
            // We need to have a [batch_size, channels, width, height] array
            val result = it.predict(image)
            val output = it.output(image)

            val message = result.joinToString(", ", prefix = "[", postfix = "]")
            println(output)
            Stats(message, result[0], output.data().asDouble().asList())
        } ?: Stats("Model not ready")
    }

    override fun saveModel(file: File): File {
        ModelSerializer.writeModel(model!!, file, true)
        return file
    }

    override fun getSamplesInTraining() = samplesUsedInTraining

    override fun getUpdateFromLayer(): ByteArray {
        val weights = model!!.getLayer(config.featureLayerIndex).params()
        val outputStream = ByteArrayOutputStream()
        Nd4j.write(outputStream, weights)
        outputStream.flush()
        val bytes = outputStream.toByteArray()
        outputStream.close()
        return bytes
    }
}