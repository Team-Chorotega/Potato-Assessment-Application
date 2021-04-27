# -*- coding: utf-8 -*-
"""
Created on Tue Apr 27 13:59:08 2021

* I did this in google colab, therefore you will need to change 
 the directories of files before running

@author: Alex (alexander.glenn@wsu.edu)
"""

!pip install tensorflow==2.1.0

import tensorflow as tf
from tensorflow import keras
from google.colab import drive
import os
drive.mount('/content/drive')


os.chdir('/content/drive/My Drive/Colab Notebooks/model_testing')

model = keras.models.load_model("potato_model_4_11_21.h5", compile=True)

# Convert the model
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save the model.
with open('potato_model_4_11_21.tflite', 'wb') as f:
  f.write(tflite_model)