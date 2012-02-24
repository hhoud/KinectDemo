/****************************************************************************
*                                                                           *
*  OpenNI 1.x Alpha                                                         *
*  Copyright (C) 2011 PrimeSense Ltd.                                       *
*                                                                           *
*  This file is part of OpenNI.                                             *
*                                                                           *
*  OpenNI is free software: you can redistribute it and/or modify           *
*  it under the terms of the GNU Lesser General Public License as published *
*  by the Free Software Foundation, either version 3 of the License, or     *
*  (at your option) any later version.                                      *
*                                                                           *
*  OpenNI is distributed in the hope that it will be useful,                *
*  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
*  GNU Lesser General Public License for more details.                      *
*                                                                           *
*  You should have received a copy of the GNU Lesser General Public License *
*  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
*                                                                           *
****************************************************************************/
package kinectdemo;

import com.googlecode.javacv.CanvasFrame;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.OpenNI.*;

import java.nio.ShortBuffer;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import kinectdemo.facedetection.DetectFace;

public class UserTracker extends Component
{
	class NewUserObserver implements IObserver<UserEventArgs>
	{
		@Override
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args)
		{
			//System.out.println("New user " + args.getId());
			try
			{
				if (skeletonCap.needPoseForCalibration())
				{
					poseDetectionCap.startPoseDetection(calibPose, args.getId());
				}
				else
				{
					skeletonCap.requestSkeletonCalibration(args.getId(), true);
				}
			} catch (StatusException e)
			{
				e.printStackTrace();
			}
		}
	}
	class LostUserObserver implements IObserver<UserEventArgs>
	{
		@Override
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args)
		{
			//System.out.println("Lost user " + args.getId());
			joints.remove(args.getId());
		}
	}
	
	class CalibrationCompleteObserver implements IObserver<CalibrationProgressEventArgs>
	{
		@Override
		public void update(IObservable<CalibrationProgressEventArgs> observable,
				CalibrationProgressEventArgs args)
		{
			System.out.println("Calibration complete: " + args.getStatus());
			try
			{
			if (args.getStatus() == CalibrationProgressStatus.OK)
			{
				//System.out.println("starting tracking "  +args.getUser());
					skeletonCap.startTracking(args.getUser());
	                joints.put(new Integer(args.getUser()), new HashMap<SkeletonJoint, SkeletonJointPosition>());
			}
			else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT)
			{
				if (skeletonCap.needPoseForCalibration())
				{
					poseDetectionCap.startPoseDetection(calibPose, args.getUser());
				}
				else
				{
					skeletonCap.requestSkeletonCalibration(args.getUser(), true);
				}
			}
			} catch (StatusException e)
			{
				e.printStackTrace();
			}
		}
	}
	class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs>
	{
		@Override
		public void update(IObservable<PoseDetectionEventArgs> observable,
				PoseDetectionEventArgs args)
		{
			//System.out.println("Pose " + args.getPose() + " detected for " + args.getUser());
			try
			{
				poseDetectionCap.stopPoseDetection(args.getUser());
				skeletonCap.requestSkeletonCalibration(args.getUser(), true);
			} catch (StatusException e)
			{
				e.printStackTrace();
			}
		}
	}
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private OutArg<ScriptNode> scriptNode;
    private Context context;
    private DepthGenerator depthGen;
    private UserGenerator userGen;
    private ImageGenerator imgGen;
    private IRGenerator irGen;
    private SkeletonCapability skeletonCap;
    private PoseDetectionCapability poseDetectionCap;
    private byte[] imgbytes;
    private float histogram[];
    String calibPose = null;
    HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;

    private boolean drawBackground = true;
    private boolean drawPixels = true;
    private boolean drawSkeleton = true;
    private boolean printID = false;
    private boolean printState = true;
    
    private static final int MIN_8_BIT = 0;
    private static final int MAX_8_BIT = 255;

    private BufferedImage bimg;
    private BufferedImage irimg;
    private IplImage iplRgbImage;
    int width, height, left, top, right, bottom, imgWidth, imgHeight;
    private boolean rgb = false;
    private final String SAMPLE_XML_FILE = "/home/hh354/kinect/OpenNI/Samples/Config/SamplesConfig.xml";
    private MapOutputMode mapMode;
    
    Thread t;
    private RGBTracker rgbT;
    private IRTracker irT;
    
    public UserTracker()
    {
        try {
            scriptNode = new OutArg<ScriptNode>();
            context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);

            depthGen = DepthGenerator.create(context);
            DepthMetaData depthMD = depthGen.getMetaData();
            mapMode = new MapOutputMode(640, 480, 30);
            irT = new IRTracker();
            //rgbT = new RGBTracker();
            setRGB(rgb);
            
            histogram = new float[10000];
            width = depthMD.getFullXRes();
            height = depthMD.getFullYRes();
            
            imgbytes = new byte[width*height*3];

            userGen = UserGenerator.create(context);
            skeletonCap = userGen.getSkeletonCapability();
            poseDetectionCap = userGen.getPoseDetectionCapability();
            
            userGen.getNewUserEvent().addObserver(new NewUserObserver());
            userGen.getLostUserEvent().addObserver(new LostUserObserver());
            skeletonCap.getCalibrationCompleteEvent().addObserver(new CalibrationCompleteObserver());
            poseDetectionCap.getPoseDetectedEvent().addObserver(new PoseDetectedObserver());
            
            calibPose = skeletonCap.getSkeletonCalibrationPose();
            joints = new HashMap<Integer, HashMap<SkeletonJoint,SkeletonJointPosition>>();
            
            skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);
            context.startGeneratingAll();
        } catch (GeneralException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    public void setRGB(boolean value){
        rgb = value;
        if(rgb){
            if(irT!= null && irT.getIsRunning())
                irT.setIsRunning(false);
            t = new Thread(rgbT);
            t.start();
        }else{
            if(rgbT!= null && rgbT.getIsRunning())
                rgbT.setIsRunning(false);
            t = new Thread(irT);
            t.start();
        }
    }
    
    private void calcHist(ShortBuffer depth)
    {
        // reset
        for (int i = 0; i < histogram.length; ++i)
            histogram[i] = 0;
        
        depth.rewind();

        int points = 0;
        while(depth.remaining() > 0)
        {
            short depthVal = depth.get();
            if (depthVal != 0)
            {
                histogram[depthVal]++;
                points++;
            }
        }
        
        for (int i = 1; i < histogram.length; i++)
        {
            histogram[i] += histogram[i-1];
        }

        if (points > 0)
        {
            for (int i = 1; i < histogram.length; i++)
            {
                histogram[i] = 1.0f - (histogram[i] / (float)points);
            }
        }
    }
    
    void updateDepth()
    {
        if(rgb){
            try {

                context.waitAnyUpdateAll();

                DepthMetaData depthMD = depthGen.getMetaData();
                SceneMetaData sceneMD = userGen.getUserPixels(0);

                ShortBuffer scene = sceneMD.getData().createShortBuffer();
                ShortBuffer depth = depthMD.getData().createShortBuffer();
                calcHist(depth);
                depth.rewind();

                while(depth.remaining() > 0)
                {
                    int pos = depth.position();
                    short pixel = depth.get();
                    short user = scene.get();

                    imgbytes[3*pos] = 0;
                    imgbytes[3*pos+1] = 0;
                    imgbytes[3*pos+2] = 0;

                    if (drawBackground || pixel != 0)
                    {
                            //define a color for the user
                            int colorID = user % (colors.length-1);
                            //if there's no user, grayscale values
                            if (user == 0)
                            {
                                colorID = colors.length-1;
                            }
                            //set the pixel color values
                            if (pixel != 0)
                            {
                                    float histValue = histogram[pixel];
                                    imgbytes[3*pos] = (byte)(histValue*colors[colorID].getRed());
                                    imgbytes[3*pos+1] = (byte)(histValue*colors[colorID].getGreen());
                                    imgbytes[3*pos+2] = (byte)(histValue*colors[colorID].getBlue());
                            }
                    }
                }
            } catch (GeneralException e) {
                e.printStackTrace();
            }
        }
    }


    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    Color colors[] = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE};
    public void getJoint(int user, SkeletonJoint joint) throws StatusException
    {
        SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user, joint);
        if (pos.getPosition().getZ() != 0)
        {
            joints.get(user).put(joint, new SkeletonJointPosition(depthGen.convertRealWorldToProjective(pos.getPosition()), pos.getConfidence()));
        }
        else
        {
            joints.get(user).put(joint, new SkeletonJointPosition(new Point3D(), 0));
        }
    }
    public void getJoints(int user) throws StatusException
    {
    	getJoint(user, SkeletonJoint.HEAD);
    	getJoint(user, SkeletonJoint.NECK);
    	
    	getJoint(user, SkeletonJoint.LEFT_SHOULDER);
    	getJoint(user, SkeletonJoint.LEFT_ELBOW);
    	getJoint(user, SkeletonJoint.LEFT_HAND);

    	getJoint(user, SkeletonJoint.RIGHT_SHOULDER);
    	getJoint(user, SkeletonJoint.RIGHT_ELBOW);
    	getJoint(user, SkeletonJoint.RIGHT_HAND);

    	getJoint(user, SkeletonJoint.TORSO);

    	getJoint(user, SkeletonJoint.LEFT_HIP);
        getJoint(user, SkeletonJoint.LEFT_KNEE);
        getJoint(user, SkeletonJoint.LEFT_FOOT);

    	getJoint(user, SkeletonJoint.RIGHT_HIP);
        getJoint(user, SkeletonJoint.RIGHT_KNEE);
        getJoint(user, SkeletonJoint.RIGHT_FOOT);

    }
    void drawLine(Graphics g, HashMap<SkeletonJoint, SkeletonJointPosition> jointHash, SkeletonJoint joint1, SkeletonJoint joint2)
    {
        Point3D pos1 = jointHash.get(joint1).getPosition();
        Point3D pos2 = jointHash.get(joint2).getPosition();

        if (jointHash.get(joint1).getConfidence() == 0 || jointHash.get(joint2).getConfidence() == 0)
            return;

        g.drawLine((int)pos1.getX(), (int)pos1.getY(), (int)pos2.getX(), (int)pos2.getY());
    }
    public void drawSkeleton(Graphics g, int user) throws StatusException
    {
    	getJoints(user);
    	/*HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints.get(new Integer(user));

    	drawLine(g, dict, SkeletonJoint.HEAD, SkeletonJoint.NECK);

    	drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.TORSO);
    	drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.TORSO);

    	drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
    	drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
    	drawLine(g, dict, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);

    	drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
    	drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW);
    	drawLine(g, dict, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);

    	drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.TORSO);
    	drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.TORSO);
    	drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP);

    	drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
    	drawLine(g, dict, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);

    	drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
    	drawLine(g, dict, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);*/
    }
    
    public void cropImage(int cTop, int cLeft, int cWidth, int cHeight, int user){
        try {
            // create destination image
            IplImage iplCrop = IplImage.create(cWidth, cHeight, IPL_DEPTH_8U, 3);

            //choose between ir or rgb
            IplImage image = (rgb)?iplRgbImage:new IplImage().createFrom(irimg);
            
            //Set ROI
            cvSetImageROI(image, cvRect(cLeft, cTop, cWidth, cHeight));

            /* copy subimage */
            cvCopy(image.roi(image.roi()), iplCrop);
            //cvCopy(iplRgbImage, iplCrop);

            /* reset the Region of Interest */
            cvResetImageROI(image);

            //Perform automatic gamma correction
            GammaCorrection gc = new GammaCorrection();
            iplCrop = gc.correctGamma(iplCrop);
            
            final DetectFace detect = new DetectFace(iplCrop.getBufferedImage());
            boolean hasFace = detect.detectFaces();
            iplCrop = IplImage.createFrom(detect.getImg());
            
            // Detect the face
            if(!hasFace){
                FaceDetection fdetect = new FaceDetection();
                iplCrop = fdetect.detectFaces(iplCrop);
            }
            
            //Save ROI
            Date date= new Date();
            cvSaveImage("faces/face_user_"+user+"_"+date.getTime()+".jpg", iplCrop);
        } catch (IOException ex) {
            Logger.getLogger(UserTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void trackUser(Graphics g, int user) throws StatusException
    {
        DepthMetaData depthMD = depthGen.getMetaData();
        SceneMetaData sceneMD = userGen.getUserPixels(0);
        SceneMap sceneMap = sceneMD.getData();
        //int[][] userMap = new int[sceneMap.getXRes()][sceneMap.getYRes()];
        int maxX = 0, minX = width, maxY = 0, minY = height, pixel=0;
        
        //find the user region
        for(int j=0;j<=sceneMap.getYRes()-1;j++){
            for(int i=0;i<=sceneMap.getXRes()-1;i++){
                if(sceneMap.readPixel(i,j)==user){
                    if(i > maxX)
                        maxX = i;
                    if(j > maxY)
                        maxY = j;

                    if(i < minX)
                        minX = i;
                    if(j < minY)
                        minY = j;
                }
                //userMap[i][j] = pixel;
                //System.out.print(sceneMap.readPixel(i,j));
            }
            //System.out.print("\r\n");
        }
        //System.out.println(" ");
        cropImage(minY, minX, maxX-minX, maxY-minY, user);
    }
    
    public void trackHead(Graphics g, int user) throws StatusException
    {   
        SceneMetaData sceneMD = userGen.getUserPixels(0);
        SceneMap sceneMap = sceneMD.getData();
        
        //find the head and torso positions
        SkeletonJointPosition headPos = joints.get(new Integer(user)).get(SkeletonJoint.HEAD);
        SkeletonJointPosition neckPos = joints.get(new Integer(user)).get(SkeletonJoint.TORSO);
        float confHead = headPos.getConfidence();
        float confNeck = neckPos.getConfidence();
        if(confHead >= .7 && confNeck >= .7){
            Point3D headPoint = headPos.getPosition();
            Point3D neckPoint = neckPos.getPosition();

            int x = (int) headPoint.getX();
            int y = (int) headPoint.getY();

            //define region around head
            //define top of head starting from head bone position
            top = y;
            boolean userPix = true;
            while(userPix){
                if(sceneMap.readPixel(x,top)==new Integer(user) && top>0){
                    top--;
                }else{
                    userPix = false;
                }
            }
            //define left of head starting from head bone position
            left = x;
            userPix = true;
            while(userPix){
                if(sceneMap.readPixel(left,y)==new Integer(user) && left>0){
                    left--;
                }else{
                    userPix = false;
                }
            }
            //define right of head starting from head bone position
            right = x;
            userPix = true;
            while(userPix){
                if(sceneMap.readPixel(right,y)==new Integer(user) && right<sceneMap.getXRes()){
                    right++;
                }else{
                    userPix = false;
                }
            }
            bottom = (int) depthGen.convertRealWorldToProjective(neckPoint).getY();
            bottom = (bottom<0)?0:bottom;

            //g.setColor(Color.RED);
            //g.drawRect(left-10, top-10, right-left+20, bottom-top+20);
            
            //high res
            //cropImage((top+10)*2, (left-10)*2, (right-left+20)*2, (bottom-top+40)*2, user);
            
            //low res
            cropImage((top+10), (left-10), (right-left+20), (bottom-top+40), user);
        }
    }
    
    CanvasFrame canvas = new CanvasFrame("Test");
    //CanvasFrame canvas2 = new CanvasFrame("Test gamma");
    public void paint(Graphics g)
    {
    	if (drawPixels)
    	{//Draw depth image
            DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height*3);
            WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null); 
            ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
            bimg = new BufferedImage(colorModel, raster, false, null);

            g.drawImage(bimg, 0, 0, null);
    	}
        try{
            if(rgb){
                canvas.showImage(rgbT.getImage());
            }else{
                canvas.showImage(new IplImage().createFrom(irT.getImage()));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        try
        {
            int[] users = userGen.getUsers();
            for (int i = 0; i < users.length; ++i)
            {
            Color c = colors[users[i]%colors.length];
            c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());

            g.setColor(c);
                if (drawSkeleton && skeletonCap.isSkeletonTracking(users[i]))
                {
                    drawSkeleton(g, users[i]);
                    //trackHead(g,users[i]);
                    //trackUser(g,users[i]);
                }
                printID=false;
                if (printID)
                {
                    Point3D com = depthGen.convertRealWorldToProjective(userGen.getUserCoM(users[i]));
                    String label = null;
                    if (!printState)
                    {
                            label = new String(""+users[i]);
                    }
                    else if (skeletonCap.isSkeletonTracking(users[i]))
                    {
                            // Tracking
                            label = new String(users[i] + " - Tracking");
                    }
                    else if (skeletonCap.isSkeletonCalibrating(users[i]))
                    {
                            // Calibrating
                            label = new String(users[i] + " - Calibrating");
                    }
                    else
                    {
                            // Nothing
                            label = new String(users[i] + " - Looking for pose (" + calibPose + ")");
                    }

                    g.drawString(label, (int)com.getX(), (int)com.getY());
                }
            }
        } catch (StatusException e)
        {
            e.printStackTrace();
        }
    }
}

