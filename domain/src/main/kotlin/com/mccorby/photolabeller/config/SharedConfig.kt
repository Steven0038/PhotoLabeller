package com.mccorby.photolabeller.config

data class SharedConfig(val imageSize: Int,
                        val channels: Int,
                        val modelFilename: String,
                        val batchSize: Int,
                        val featureLayerIndex: Int = 3,
                        val labels: List<String> = listOf("airplane", "automobile", "bird", "cat", "deer", "dog", "frog", "horse", "ship", "truck"),//TODO
//                        val labels: List<String> = listOf("daisy", "dandelion", "roses", "sunflowers", "tulips"),//TODO
                        val maxSamples: Int = 32,
                        val maxEpochs: Int = 2)