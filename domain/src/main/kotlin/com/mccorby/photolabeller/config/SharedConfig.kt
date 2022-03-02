package com.mccorby.photolabeller.config

data class SharedConfig(val imageSize: Int,
                        val channels: Int,
                        val modelFilename: String,
                        val batchSize: Int,
                        val featureLayerIndex: Int = 3,
                        /**
                         * TODO customized model labels only allow lowercase
                         */
//                        val labels: List<String> = listOf("airplane", "automobile", "bird", "cat", "deer", "dog", "frog", "horse", "ship", "truck"), // cifar10
//                        val labels: List<String> = listOf("cloudy", "rain", "shine", "sunrise"), // multiclass-weather-dataset
//                        val labels: List<String> = listOf("dew", "fogsmog", "frost", "glaze", "hail", "lightning", "rain", "rainbow", "rime", "sandstorm", "snow"), // Harvard Weather Image Recognition
                        val labels: List<String> = listOf("cloudy", "foggy", "rainy", "snowy", "sunny"), // SP Weather
                        val maxSamples: Int = 32,
                        val maxEpochs: Int = 2)