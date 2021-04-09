# -*- coding: utf-8 -*-
"""
Created on Fri Apr  2 12:24:54 2021

This script outputs 6 items in this order:

1. array containing data for each potato in image (convert to java array using Chaquopy)
2. number of potatoes (string)
3. minimum l/w ratio (string)
4. maximum l/w ratio (string)
5. average l/w ratio (string)
6. processed image (encoded as a string, decode using Chaquopy)

The main() function contains two input parameters: The encoded image from 
Chaquopy and a string specifying the coin reference object being used. At this
time the only options are "Quarter", "Dime", or "None". Measurements are in centimeters.
If no reference object is used, the measurements will be returned in pixels.
There is also a good chance it may not work well due to no size thresholding if
no reference object is used.

            
@author: Alex (alexander.glenn@wsu.edu)
"""

#-----------------------------------------------------------
# These libraries need to be imported in Chaquopy: numpy, opencv, imutils, PIL, skimage
import numpy as np
import cv2 
import imutils
import imutils.contours
from PIL import Image
import base64
import io
import skimage
import skimage.segmentation
#-----------------------------------------------------------

def text_scaling(input_img):
    font_size = int((5*min(input_img.shape[0], input_img.shape[1])/1370))
    font_thickness = int((12*min(input_img.shape[0], input_img.shape[1])/1370))
    line_thickness = int((15*min(input_img.shape[0], input_img.shape[1])/1370))
    return font_size, font_thickness, line_thickness

def coin_type(string):
    if string == "Quarter":
        diameter = 2.426 #cm
    if string == "Dime":
        diameter = 1.791 #cm
    return diameter

def ref_obj_analysis(coin_string, markers):
    
    normalized_mask = cv2.normalize(markers, None, 0, 255, cv2.NORM_MINMAX, cv2.CV_8U)

    ret, full_marker_mask = cv2.threshold(normalized_mask, 0, 255, cv2.THRESH_BINARY)
    
    full_marker_mask = skimage.segmentation.clear_border(full_marker_mask)
    
    cnts = cv2.findContours(full_marker_mask.copy(), cv2.RETR_EXTERNAL,
    	cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)
    
    (cnts, _) = imutils.contours.sort_contours(cnts) # sort from left to right
    
    j = 0
    
    while(cnts[j].any): # find roundest, left-most object
        rectangle = cv2.minAreaRect(cnts[j]) # fit rectangle to contour of coin
        coin_h = rectangle[1][0]
        coin_w = rectangle[1][1]
        coin_diameter = (coin_h+coin_w)/2
        
        coin_area_calc = np.pi*(coin_diameter/2)*(coin_diameter/2)
        coin_area = cv2.contourArea(cnts[j])
        
        if(np.std([int(coin_area_calc), int(coin_area)]) <= 200): # determining roundness
            break
    
    coin_box = cv2.boxPoints(rectangle)
    coin_box = np.int0(coin_box)

    # using the reference object, determine the pixels per cm in the image
    diameter = coin_type(coin_string)
    
    pixels_per_cm = coin_diameter/diameter
    
    return pixels_per_cm, coin_box, coin_area



def main(data, coin_string):
    
    decoded_img = base64.b64decode(data)
    np_img = np.fromstring(decoded_img, np.uint8)
    
    input_img = cv2.imdecode(np_img, cv2.IMREAD_UNCHANGED)
    output_img = input_img.copy()
    
    
    #-----------------------------------------------------------
    """
    preprocessing before implementation of Watershed algorithm
    """
    
    # turn into grayscale image
    img_gray = cv2.cvtColor(input_img, cv2.COLOR_BGR2GRAY)
    
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
    markers = skimage.segmentation.watershed(-dist_transform, markers)
    
    #-----------------------------------------------------------
    """
    If a reference object is chosen to be used,
    create a mask of the full marker image and find the reference object
    the reference object should be the left-most object in the image
    """
    
    if(coin_string != "None"):
        pixels_per_cm, coin_box, coin_area = ref_obj_analysis(coin_string, markers)
        
    #-----------------------------------------------------------
    """
    again use the markers, but this time go through them individually
    to fit bounding rectangles and extract the needed data from the image
    """
    
    font_size, font_thickness, line_thickness = text_scaling(input_img)
    
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
        
                cv2.putText(output_img,str(potato_number), (x,y), 
                            cv2.FONT_HERSHEY_SIMPLEX, font_size, (255,120,0), font_thickness)
                
                potato_arr.append([np.round(h,2),np.round(w,2),np.round(h/w,2)])
                
                potato_number = potato_number+1
                
            cv2.drawContours(output_img,[coin_box],-1,(255,0,255),line_thickness)  
        
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
        
            cv2.putText(output_img,str(potato_number), (x,y), 
                        cv2.FONT_HERSHEY_SIMPLEX, font_size, (255,120,0), font_thickness)
            
            potato_arr.append([np.round(h,2),np.round(w,2),np.round(h/w,2)])
            
            potato_number = potato_number+1
            
    #-----------------------------------------------------------
    """
    after data is collected, finalize what will be outputted:
        1. array containing data for each potato in image (convert to java array using Chaquopy)
        2. number of potatoes (string)
        3. minimum l/w ratio (string)
        4. maximum l/w ratio (string)
        5. average l/w ratio (string)
        6. processed image (encoded as a string, decode using Chaquopy)
    """
    
    # check if arrays are empty
    if not potato_arr:
        potato_arr = [[0,0,0]]
        
    if not ratio_arr:
        ratio_arr = [0]

    ratio_arr = np.array(ratio_arr)
    
    num = len(ratio_arr)
    min_ratio = np.round(ratio_arr.min(), 2)
    max_ratio = np.round(ratio_arr.max(), 2)
    avg_ratio = np.round(np.sum(ratio_arr)/num, 2)
    
    fixed_output_img = cv2.cvtColor(output_img, cv2.COLOR_BGR2RGB)
    
    pil_img = Image.fromarray(fixed_output_img)
    buff = io.BytesIO()
    pil_img.save(buff, format="JPEG")
    
    output_img_str = base64.b64encode(buff.getvalue())
    
    return potato_arr, ""+str(num), ""+str(min_ratio), ""+str(max_ratio), ""+str(avg_ratio), ""+str(output_img_str, 'utf-8')
