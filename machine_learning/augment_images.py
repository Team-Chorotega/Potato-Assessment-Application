# -*- coding: utf-8 -*-
"""
Created on Tue Apr 27 13:59:52 2021

* I did this in google colab, therefore you will need to change 
 the directories of files before running

* Put each code block in a seperate block in colab (seperated by ---)

Code partially inspired by https://github.com/bnsreenu/python_for_microscopists/blob/master/177_albumentations_aug.py

@author: Alex (alexander.glenn@wsu.edu)
"""
# -------------------------------------------------------------------
%matplotlib inline

import numpy as np
import matplotlib.pyplot as plt
from skimage.transform import AffineTransform, warp
from skimage import io, img_as_ubyte
import random
import glob
import os
import cv2
from scipy.ndimage import rotate
from google.colab import drive
drive.mount('/content/drive')

import albumentations as A

train_image_dir = '/content/drive/My Drive/Colab Notebooks/data_extended_4_1_21/data_extended_train'
train_mask_dir = '/content/drive/My Drive/Colab Notebooks/data_extended_4_1_21/data_extended_masks'
img_augmented_path = '/content/drive/My Drive/Colab Notebooks/data_extended_4_1_21/data_extended_train_aug'
msk_augmented_path = '/content/drive/My Drive/Colab Notebooks/data_extended_4_1_21/data_extended_masks_aug'

train_images = []
train_masks = []

os.chdir(train_image_dir)
for img_name in glob.glob('*'):
  img = cv2.imread(img_name)
  os.chdir(train_mask_dir)
  for mask_name in glob.glob(img_name+'*'):
    mask = cv2.imread(mask_name, 0)
    train_masks.append(mask)
    train_images.append(img)
  os.chdir(train_image_dir)

train_images = np.array(train_images)
train_masks = np.array(train_masks)
# -------------------------------------------------------------------
aug = A.Compose([           
    A.RandomRotate90(p=0.8),
    A.HorizontalFlip(p=0.5),
    A.VerticalFlip(p=0.5),
    A.RandomBrightnessContrast(p=0.2),
    A.Blur(blur_limit=3,p=0.3)
    ]
)

images_to_generate=160
i=1   # variable to iterate till images_to_generate


while i<=images_to_generate: 
    number = random.randint(0, len(train_images)-1)  #Pick a number to select an image & mask
    image = train_images[number]
    mask = train_masks[number]
    #print(image, mask)
    #image=random.choice(images) #Randomly select an image name
    original_image = image
    original_mask = mask
    
    """
    fig1, ax1 = plt.subplots()
    ax1.imshow(cv2.cvtColor(original_image,cv2.COLOR_BGR2RGB))

    fig, ax = plt.subplots()
    ax.imshow(original_mask)
    """

    augmented = aug(image=cv2.cvtColor(original_image,cv2.COLOR_BGR2RGB), mask=original_mask)
    transformed_image = augmented['image']
    transformed_mask = augmented['mask']
    
    """
    fig2, ax2 = plt.subplots()
    ax2.imshow(cv2.cvtColor(transformed_image,cv2.COLOR_BGR2RGB))

    fig3, ax3 = plt.subplots()
    ax3.imshow(transformed_mask)
    """

    
    new_image_path= "%s/augmented_image_%s.png" %(img_augmented_path, i+160)
    new_mask_path = "%s/augmented_mask_%s.png" %(msk_augmented_path, i+160)
    io.imsave(new_image_path, transformed_image)
    io.imsave(new_mask_path, transformed_mask)
    
    
    i =i+1