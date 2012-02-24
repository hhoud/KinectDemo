/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kinectdemo;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

public class FaceDetection {
 
  // The cascade definition to be used for detection.
  private static final String CASCADE_FILE = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_frontalface_alt.xml";
  //private static final String CASCADE_FILE = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_profileface.xml";
  
  public static void main(String[] args) throws Exception {
      
  }
  
  public IplImage detectFaces(IplImage originalImage){
    // We need a grayscale image in order to do the recognition, so we
    // create a new image of the same size as the original one.
    IplImage grayImage = IplImage.create(originalImage.width(),
    originalImage.height(), IPL_DEPTH_8U, 1);
 
    // We convert the original image to grayscale.
    cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
 
    CvMemStorage storage = CvMemStorage.create();
 
    // We instantiate a classifier cascade to be used for detection, using the cascade definition.
    CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(
    cvLoad(CASCADE_FILE));
 
    // We detect the faces.
    CvSeq faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 1, 0);
 
    //We iterate over the discovered faces and draw yellow rectangles around them.
    for (int i = 0; i < faces.total(); i++) {
      CvRect r = new CvRect(cvGetSeqElem(faces, i));
      cvRectangle(originalImage, cvPoint(r.x(), r.y()),
      cvPoint(r.x() + r.width(), r.y() + r.height()), CvScalar.YELLOW, 1, CV_AA, 0);
    }
 
    // Save the image to a new file.
    //cvSaveImage(args[1], originalImage);
    return originalImage;
  }
}