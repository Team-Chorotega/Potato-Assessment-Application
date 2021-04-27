# -*- coding: utf-8 -*-
"""
Created on Tue Apr 27 13:59:33 2021

* I did this in google colab, therefore you will need to change 
 the directories of files before running

* Put each code block in a seperate block in colab (seperated by ---)

Code partially inspired by https://github.com/bnsreenu/python_for_microscopists/blob/master/177_semantic_segmentation_made_easy_using_segm_models.py

@author: Alex (alexander.glenn@wsu.edu)
"""
# -------------------------------------------------------------------
!pip install segmentation-models
!pip install tensorflow==2.1.0
# -------------------------------------------------------------------
%matplotlib inline
import tensorflow as tf
import segmentation_models as sm
import glob
import cv2
import os
import numpy as np
import matplotlib.pyplot as plt
from google.colab import drive
drive.mount('/content/drive')
# -------------------------------------------------------------------
BACKBONE = 'resnet34'
preprocess_input = sm.get_preprocessing(BACKBONE)

# resnet34
SIZE_X = 256
SIZE_Y = 256
# -------------------------------------------------------------------
train_image_dir = '/content/drive/My Drive/Colab Notebooks/potato_data_v2/potato_train_v2'
train_mask_dir = '/content/drive/My Drive/Colab Notebooks/potato_data_v2/potato_masks_v2/Potato AI'

train_images = []
train_masks = []

os.chdir(train_image_dir)
for img_name in glob.glob('*'):
  img = cv2.imread(img_name, cv2.IMREAD_COLOR)
  img = cv2.resize(img,(SIZE_Y, SIZE_X))
  img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
  os.chdir(train_mask_dir)
  for mask_name in glob.glob(img_name+'*'):
    mask = cv2.imread(mask_name, 0)
    mask = cv2.resize(mask,(SIZE_Y, SIZE_X))
    train_masks.append(mask)
    train_images.append(img)
  os.chdir(train_image_dir)
# -------------------------------------------------------------------
# if augmented images are used (they should be used)
"""
train_image_aug_dir = '/content/drive/My Drive/Colab Notebooks/potato_data_v2/potato_train_v2_aug'
train_mask_aug_dir = '/content/drive/My Drive/Colab Notebooks/potato_data_v2/potato_masks_v2_aug'

i = 1

while i<=160:
  os.chdir(train_image_aug_dir)
  img = cv2.imread('augmented_image_'+str(i)+'.png', cv2.IMREAD_COLOR)
  img = cv2.resize(img,(SIZE_Y, SIZE_X))
  img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)

  os.chdir(train_mask_aug_dir)
  mask = cv2.imread('augmented_mask_'+str(i)+'.png', 0)
  mask = cv2.resize(mask,(SIZE_Y, SIZE_X))
  
  train_masks.append(mask)
  train_images.append(img)

  i = i+1
"""
# -------------------------------------------------------------------
train_images = np.array(train_images)
train_masks = np.array(train_masks)

# x = images, y = masks

x = train_images
y = train_masks
y = np.expand_dims(y, axis=3)

from sklearn.model_selection import train_test_split

# try different random states (using 42)
x_train, x_val, y_train, y_val = train_test_split(x, y, test_size = 0.2, random_state=42)

x_train = preprocess_input(x_train)
x_val = preprocess_input(x_val)

print(x_train.shape)
print(y_train.shape)
print(x_val.shape)
print(y_val.shape)
# -------------------------------------------------------------------
model = sm.Unet(BACKBONE, input_shape=(256, 256, 3), encoder_weights='imagenet')
model.compile('Adam', loss='binary_crossentropy', metrics=['mse'])
# -------------------------------------------------------------------
history=model.fit(x_train, y_train, epochs=31, verbose=1, validation_data=(x_val, y_val))
history.history.keys()
# -------------------------------------------------------------------
plt.plot(history.history['loss'])
plt.plot(history.history['val_loss'])
plt.title('Model loss')
plt.ylabel('Loss')
plt.xlabel('Epoch')
plt.legend(['Train', 'Test'], loc='upper left')
plt.show()
# -------------------------------------------------------------------
os.chdir('/content/drive/My Drive/Colab Notebooks/model_testing')
model.save('potato_model_4_11_21.h5')
# -------------------------------------------------------------------
from tensorflow import keras
model = keras.models.load_model('potato_model_4_11_21.h5', compile=False)

test_img = cv2.imread('1012TRT05.JPG', cv2.IMREAD_COLOR)
fig, ax = plt.subplots()
ax.imshow(cv2.cvtColor(test_img, cv2.COLOR_BGR2RGB))
origx = test_img.shape[0]
origy = test_img.shape[1]

test_img = cv2.resize(test_img, (SIZE_Y, SIZE_X))
test_img = cv2.cvtColor(test_img, cv2.COLOR_RGB2BGR)
test_img = np.expand_dims(test_img, axis=0)

prediction = model.predict(test_img)

#View and save segmented image
prediction_image = prediction.reshape(mask.shape)
newimg = cv2.resize(prediction_image, (origy, origx))

fig1, ax1 = plt.subplots()
ax1.imshow(newimg, cmap='gray')