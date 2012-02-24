/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kinectdemo;

import static com.googlecode.javacv.cpp.opencv_core.*;
import java.awt.image.BufferedImage;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;

import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 *
 * @author hh354
 */
public class GammaCorrection {
    int width;
    int height;
    
    public IplImage correctGamma(IplImage img){
        //get image intensity
        //img.applyGamma(d);
        IplImage gray = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, 1);
        cvCvtColor(img, gray, CV_RGB2GRAY);
        BufferedImage buffImg = img.getBufferedImage();
        BufferedImage buffGray = gray.getBufferedImage();
        double grayArr[]=new double[gray.width()*gray.height()];
        int counter=0;
        for (int i=0; i<gray.width(); i++) {
            for(int j=0;j<gray.height();j++){
                grayArr[counter] = buffGray.getRGB(i, j);
                counter++;
            }
        }
        
        double imgSd = new StandardDeviation().evaluate(grayArr);
        double imgMean = new Mean().evaluate(grayArr);
        double y=0;
        if(imgMean > 0.5)
            y = 1 + (Math.abs(0.5-imgMean)/imgSd);
        else
            y = 1/(1 + (Math.abs(0.5-imgMean)/imgSd));
    
        img.applyGamma(y);
        
        return img;
    }
}
