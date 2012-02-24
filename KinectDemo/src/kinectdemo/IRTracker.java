/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kinectdemo;


import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.OpenNI.Context;
import org.OpenNI.GeneralException;
import org.OpenNI.IRGenerator;
import org.OpenNI.IRMetaData;
import org.OpenNI.MapOutputMode;
import org.OpenNI.OutArg;
import org.OpenNI.ScriptNode;
import org.OpenNI.StatusException;

/**
 *
 * @author hh354
 */
public class IRTracker implements Runnable{
    
    private OutArg<ScriptNode> scriptNode;
    private Context context;
    private final String SAMPLE_XML_FILE = "/home/hh354/kinect/OpenNI/Samples/Config/SamplesConfig.xml";
    private IRGenerator irGen;
    private static final int MIN_8_BIT = 0;
    private static final int MAX_8_BIT = 255;
    private BufferedImage irImg;
    private int imgWidth, imgHeight;
    private boolean isRunning = true;
    
    public IRTracker()
    {
    }
    
    private void configOpenNI(){
        try {
            scriptNode = new OutArg<ScriptNode>();
            context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);
            MapOutputMode mapMode = new MapOutputMode(640, 480, 30);
            irGen = IRGenerator.create(context);
            irGen.setMapOutputMode(mapMode);
            IRMetaData irMD = irGen.getMetaData();
            imgWidth = irMD.getFullXRes();
            imgHeight = irMD.getFullYRes();
            context.setGlobalMirror(true);
            context.startGeneratingAll();
        } catch (GeneralException ex) {
            Logger.getLogger(IRTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     public void run()
    {
        isRunning = true;
        configOpenNI();
        while (isRunning) {
            try {
                context.waitAnyUpdateAll();
                //context.waitOneUpdateAll(imageGen);
                // wait for 'one' ('any' is safer)
            }
            catch(StatusException e)
            { 
                System.out.println(e);
                System.exit(1);
            }
            //long startTime = System.currentTimeMillis();
            updateIRImage();
            //imageCount++;
            //totalTime += (System.currentTimeMillis() - startTime);
        }
        // close down
        try {
            context.stopGeneratingAll();
        }
        catch (StatusException e) {}
        context.release();
        System.exit(1);
    } // end of run()
    
    private BufferedImage createGrayIm(ShortBuffer irSB, int minIR, int maxIR)
    {
        // create a grayscale image
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
        // access the image's data buffer
        byte[] data = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
        float displayRatio = (float)(MAX_8_BIT - MIN_8_BIT)/(maxIR - minIR);
        // scale the converted IR data over the grayscale range;
        int i = 0;
        while (irSB.remaining() > 0) {
            int irVal = irSB.get();
            int out;
            if (irVal <= minIR)
                out = MIN_8_BIT;
            else if (irVal >= maxIR)
                out = MAX_8_BIT;
            else
                out = (int) ((irVal - minIR)* displayRatio);
            data[i++] = (byte) out;
            // store in the data buffer
        }
        return image;
    } 
    void updateIRImage()
    {
        try {
            ShortBuffer irSB = irGen.getIRMap().createShortBuffer();
            // scan the IR data, storing the min and max values
            int minIR = irSB.get();
            int maxIR = minIR;
            while (irSB.remaining() > 0) {
                int irVal = irSB.get();
                if (irVal > maxIR)
                    maxIR = irVal;
                if (irVal < minIR)
                    minIR = irVal;
            }
            irSB.rewind();
            // convert the IR values into 8-bit grayscales
            irImg = createGrayIm(irSB, minIR, maxIR);
        }
        catch(GeneralException e)
        { 
            System.out.println(e); 
        }
    }
    
    public BufferedImage getImage(){
        return this.irImg;
    }
    public boolean getIsRunning(){
        return this.isRunning;
    }
    public void setIsRunning(boolean running){
        this.isRunning = running;
    }
}
