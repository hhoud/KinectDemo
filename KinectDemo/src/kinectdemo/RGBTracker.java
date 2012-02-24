/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kinectdemo;

import com.googlecode.javacv.CanvasFrame;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.OpenNI.Context;
import org.OpenNI.GeneralException;
import org.OpenNI.ImageGenerator;
import org.OpenNI.ImageMap;
import org.OpenNI.ImageMetaData;
import org.OpenNI.License;
import org.OpenNI.MapOutputMode;
import org.OpenNI.NativeAccess;
import org.OpenNI.PixelFormat;
import org.OpenNI.StatusException;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 *
 * @author hh354
 */
public class RGBTracker implements Runnable {
    
    // globals
    private Context context;
    private ImageGenerator imageGen;
    private BufferedImage image;
    private int imWidth, imHeight;
    private boolean isRunning = true;
    private int imageCount = 0;
    private long totalTime = 0;
    private IplImage iplRgbImage, iplBgrImage;
    
    private void configOpenNI()
    // create context and image generator
    {
        try {
            context = new Context();
            // add the NITE License
            License license = new License("PrimeSense",
            "0KOIk2JeIBYClPWVnMoRKn5cdY4=");
            context.addLicense(license);
            // vendor, key
            imageGen = ImageGenerator.create(context);
            MapOutputMode mapMode = new MapOutputMode(640, 480, 30);
            // xRes, yRes, FPS
            imageGen.setMapOutputMode(mapMode);
            imageGen.setPixelFormat(PixelFormat.RGB24);
            // set Mirror mode for all
            context.setGlobalMirror(true);

            System.out.println("Started context generating...");
            ImageMetaData imageMD = imageGen.getMetaData();
            imWidth = imageMD.getFullXRes();
            imHeight = imageMD.getFullYRes();
            context.startGeneratingAll();
        }
        catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    } // end of configOpenNI()
    
    public void run(){
        configOpenNI();
        while(isRunning){
            try {
                // create image
                iplRgbImage = IplImage.create(imWidth, imHeight, IPL_DEPTH_8U, 3);
                iplBgrImage = IplImage.create(imWidth, imHeight, IPL_DEPTH_8U,3);
                
                // fill image
                ImageMap imageMap = imageGen.getImageMap();
                ByteBuffer byteBuffer = iplRgbImage.getByteBuffer();
                long ptr = imageMap.getNativePtr();
                NativeAccess.copyToBuffer(byteBuffer, ptr, 640*480*3);
                cvCvtColor(iplRgbImage, iplBgrImage, CV_RGB2BGR);
                byteBuffer.rewind();
            } catch (GeneralException ex) {
                Logger.getLogger(RGBTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public IplImage getImage(){
        return this.iplBgrImage;
    }
    public boolean getIsRunning(){
        return this.isRunning;
    }
    public void setIsRunning(boolean running){
        this.isRunning = running;
    }
}