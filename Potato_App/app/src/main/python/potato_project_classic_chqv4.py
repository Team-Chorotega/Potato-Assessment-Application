# -*- coding: utf-8 -*-
"""
Created on Fri Apr  2 12:24:54 2021

This script outputs 6 items in this order:

1. array containing data for each potato in image
2. number of potatoes (string)
3. minimum l/w ratio (string)
4. maximum l/w ratio (string)
5. average l/w ratio (string)
6. processed image bitmap

The main() function contains two input parameters: The encoded image from 
Chaquopy and a string specifying the coin reference object being used. Measurements are in centimeters.
If no reference object is used, the measurements will be returned in pixels.

            
@author: Alex (alexander.glenn@wsu.edu)
"""

#-----------------------------------------------------------
# These libraries need to be imported in Chaquopy: numpy, opencv, imutils, PIL, skimage, tflite-runtime
import numpy as np
import cv2 
import imutils
import imutils.contours
from PIL import Image
import io
import skimage
import skimage.segmentation
import skimage.morphology
#-----------------------------------------------------------

def text_scaling(input_img):
    font_size = int((4*min(input_img.shape[0], input_img.shape[1])/1370))
    font_thickness = int((11*min(input_img.shape[0], input_img.shape[1])/1370))
    line_thickness = int((15*min(input_img.shape[0], input_img.shape[1])/1370))
    return font_size, font_thickness, line_thickness

def coin_type(string):
    
    if string == "Quarter":
        diameter = 2.426 #cm
    elif string == "Dime":
        diameter = 1.791 #cm
    elif string[0] == "C":
        diameter = float(string.split("_")[1]) #cm

    return diameter

def ref_obj_analysis(coin_string, markers):
    
    normalized_mask = cv2.normalize(markers, None, 0, 255, cv2.NORM_MINMAX, cv2.CV_8U)

    ret, full_marker_mask = cv2.threshold(normalized_mask, 0, 255, cv2.THRESH_BINARY)
    
    full_marker_mask = skimage.segmentation.clear_border(full_marker_mask)
    
    cnts = cv2.findContours(full_marker_mask, cv2.RETR_EXTERNAL,
    	cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)
    
    j = 0
    x_circle = 0
    y_circle = 0
    circle_radius = 0
    coin_area = 0
    pixels_per_cm = 0
    
    if(cnts):
        (cnts, _) = imutils.contours.sort_contours(cnts) # sort from left to right
    
        (x_circle, y_circle), circle_radius = cv2.minEnclosingCircle(cnts[j]) 
        
        coin_diameter = 2*circle_radius
        
        coin_area = cv2.contourArea(cnts[j])
        
        # using the reference object, determine the pixels per cm in the image
        diameter = coin_type(coin_string)
    
        pixels_per_cm = coin_diameter/diameter
    
    return pixels_per_cm, x_circle, y_circle, circle_radius, coin_area



def main(data, coin_string):
    
    np_img = np.asarray(data, np.uint8)
    
    input_img = cv2.imdecode(np_img, cv2.IMREAD_UNCHANGED)
    
    # shrink image if too large (android phones can't process large images well, the newer models can though)
    if(input_img.size > 2800000):
        input_max = np.max(input_img.shape)
        input_scale = 1000/input_max
        input_img = cv2.resize(input_img, None, fx=input_scale,fy=input_scale,interpolation=cv2.INTER_AREA)
        output_img = input_img
    else:
        output_img = input_img
    
    #-----------------------------------------------------------
    """
    preprocessing before implementation of Watershed algorithm
    """
    
    # turn into grayscale image
    img_gray = cv2.cvtColor(output_img, cv2.COLOR_BGR2GRAY)
    
    # binary thresholding
    blurred_img = cv2.GaussianBlur(img_gray, (3,3), 0)
    ret, thresh = cv2.threshold(blurred_img, 0, 255, 
                                cv2.THRESH_BINARY+cv2.THRESH_OTSU)
    
    # opening (erosion followed by dilation of binary image)
    kernel = np.ones((3,3), np.uint8)
    opening = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel, iterations = 1)
    opening = skimage.segmentation.clear_border(opening) # ignore objects touching boundaries 
    
    # Watershed needs a sure background, sure foreground, and unknown pixels
    sure_bg = cv2.dilate(opening, kernel, iterations = 2)
    
    dist_transform = cv2.distanceTransform(opening, cv2.DIST_L2, 3)
    
    ret2, sure_fg = cv2.threshold(dist_transform, 0.2*dist_transform.max(), 255 ,0) # 0.2 = 20%
    
    sure_fg = np.uint8(sure_fg)
    
    unknown = cv2.subtract(sure_bg, sure_fg)
    
    # Watershed needs markers, construct using the sure foreground
    ret2, markers = cv2.connectedComponents(sure_fg)
    
    # By default marker has value of 0, and sure_bg is also 0, so make sure_bg 1
    markers = markers+1
    
    # Create a marker array
    markers[unknown==255] = 0
    
    # Implement watershed
    markers = skimage.morphology.watershed(-dist_transform, markers)
    
    #-----------------------------------------------------------
    """
    If a reference object is chosen to be used,
    create a mask of the full marker image and find the reference object
    the reference object should be the left-most object in the image
    """
    pixels_per_cm = 0
    
    if(coin_string != "None"):
        pixels_per_cm, x_circle, y_circle, circle_radius, coin_area = ref_obj_analysis(coin_string, markers)
        
    if(pixels_per_cm == 0):
        coin_string = "None"
        
    #-----------------------------------------------------------
    """
    again use the markers, but this time go through them individually
    to fit bounding rectangles and extract the needed data from the image
    """
    
    font_size, font_thickness, line_thickness = text_scaling(output_img)
    
    potato_arr = []
    ratio_arr = []
    potato_number = 1
    
    for marker in np.unique(markers):
    
        if marker == 1: # 1 means background, ignore it
            continue
        
        marker_mask = np.zeros_like(thresh)
        marker_mask[markers == marker] = 255
        
        cnts = cv2.findContours(marker_mask, 
                                cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        cnts = imutils.grab_contours(cnts)
        
        if (coin_string != "None"):
            
            if cv2.contourArea(cnts[0]) > coin_area+50: # size thresholding
            
                rectangle = cv2.minAreaRect(cnts[0]) # fit rectangle to contour
                h = (rectangle[1][0])/pixels_per_cm
                w = (rectangle[1][1])/pixels_per_cm
                
                if (w > h):
                    temp = w
                    w = h
                    h = temp
                
                ratio_arr.append(h/w)
                
                box = cv2.boxPoints(rectangle)
                box = np.int0(box)
                
                x = int(rectangle[0][0])
                y = int(rectangle[0][1])
                
                cv2.drawContours(output_img,[box],-1,(0,0,255),line_thickness)
        
                x_offset = cv2.getTextSize(str(potato_number),
                                           cv2.FONT_HERSHEY_SIMPLEX,font_size,
                                           font_thickness)[0][0]
                
                x_offset = int(x_offset/2)
        
                cv2.putText(output_img,str(potato_number), (x-x_offset,y), 
                            cv2.FONT_HERSHEY_SIMPLEX, font_size, (255,120,0), font_thickness)                
                
                potato_arr.append([np.round(h,2),np.round(w,2),np.round(h/w,2)])
                
                potato_number = potato_number+1
                
            output_img = cv2.circle(output_img, (int(x_circle), int(y_circle)), int(circle_radius), (255,0,255), line_thickness)
        
        else:
            
            rectangle = cv2.minAreaRect(cnts[0])
            h = (rectangle[1][0])
            w = (rectangle[1][1])
            
            if (w > h):
                temp = w
                w = h
                h = temp
            
            ratio_arr.append(h/w)
            
            box = cv2.boxPoints(rectangle)
            box = np.int0(box)
            
            x = int(rectangle[0][0])
            y = int(rectangle[0][1])
            
            cv2.drawContours(output_img,[box],-1,(0,0,255),line_thickness)
        
            x_offset = cv2.getTextSize(str(potato_number),
                                           cv2.FONT_HERSHEY_SIMPLEX,font_size,
                                           font_thickness)[0][0]
                
            x_offset = int(x_offset/2)
        
            cv2.putText(output_img,str(potato_number), (x-x_offset,y), 
                            cv2.FONT_HERSHEY_SIMPLEX, font_size, (255,120,0), font_thickness)            
            
            potato_arr.append([np.round(h,2),np.round(w,2),np.round(h/w,2)])
            
            potato_number = potato_number+1
            
    #-----------------------------------------------------------
    """
    after data is collected, finalize what will be outputted:
        1. array containing data for each potato in image
        2. number of potatoes (string)
        3. minimum l/w ratio (string)
        4. maximum l/w ratio (string)
        5. average l/w ratio (string)
        6. processed image
    """
    
     # check if arrays are empty
    if not potato_arr:
        potato_arr = [[0,0,0]]
        
    if not ratio_arr:
        ratio_arr = [0]
    
    ratio_arr = np.array(ratio_arr)
    potato_arr = np.array(potato_arr)
    
    num = float(len(ratio_arr))
    min_ratio = np.round(ratio_arr.min(), 2)
    max_ratio = np.round(ratio_arr.max(), 2)
    avg_ratio = np.round(np.sum(ratio_arr)/num, 2)
    
    num_str = f"{num: .2f}"
    min_str = f"{min_ratio: .2f}"
    max_str = f"{max_ratio: .2f}"
    avg_str = f"{avg_ratio: .2f}"
    
    fixed_output_img = cv2.cvtColor(output_img, cv2.COLOR_BGR2RGB)
    
    pil_img = Image.fromarray(fixed_output_img)
    buff = io.BytesIO()
    pil_img.save(buff, format="JPEG")
    
    return potato_arr, num_str+"", min_str+"", max_str+"", avg_str+"", buff.getvalue()
