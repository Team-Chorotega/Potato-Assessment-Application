# Potato-Assessment-Application
This application is being developed for Washington State University potato research and breeding programs. The application assesses potato tubers specified by the user via images.

![plot](https://github.com/Team-Chorotega/Potato-Assessment-Application/blob/main/Chorotega%20Senior%20Design%20Poster.png)


## Current Features
Please see the TuberRuler_QuickGuide.pdf for more information on how to use the application

* The left button is used to select an image from photos.
* The center button initiates the processing and analysis of the image.
* The right button resets the application.
* The options gear in the top right allows the user to select a reference object (Quarter, Dime, or None), and a processing method (Classic or Machine Learning).
  * Note that if no reference object is used, the image processing will have difficulties with size thresholding and the data will be returned in units of pixels rather than centimeters.

## Future Plans
* User interface overhaul.
* Storing of processed data.
* Improvements to the image processing algorithms.


## User Instructions
* The machine learning model must be downloaded using the link provided and put into the "python" folder of the application before use.
* Currently the potatos must be set against a dark background with good lighting. 
* The reference object must be the left-most object in the image.

If working on the app please contact Alexander Glenn about licensing
