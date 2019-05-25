package ssl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import ssl.MessagesRobocupSslDetection.SSL_DetectionFrame;
import ssl.MessagesRobocupSslDetection.SSL_DetectionRobot;
import ssl.MessagesRobocupSslWrapper.SSL_WrapperPacket;

import javafx.util.Pair;

/**
 * This file implements a multicast listener for ssl-vision packages and stores them.
 * So far we only support a single camera and no smoothing is done of the robot position
 * 
 * USAGE:
 * 	 call initListener() to start listening
 *   call closeListener() to stop listening
 *   call 
 * 
 * @author bucef
 *
 */

public class SSLListener {
	
	static InetAddress 	   group;
	static MulticastSocket socket;
	
	static final String VISION_ADDRESS = "224.5.23.2";
	static final String CMAC1_IP = "131.247.14.105";
	static final int PORT = 10006;
	static final int BUFFER_SIZE = 4096;
	
	static HashMap<String, Pair<SSL_DetectionRobot,Double>> detections = new HashMap();
	
	static Boolean closeListener = false;
	static Thread listenerThread = null;
	
	static Double timeOffset = 0.0;
	
	
	/***
	 * Function to calculate the difference between the time of the clocks
	 * @param printResults 
	 * @return
	 */
	static public boolean setOffset(boolean printResults) {
		return setOffset(CMAC1_IP, printResults);
	}
	
	
	/***
	 * Function to calculate the difference between the time of the clocks
	 * @param ftpServerIP   IP of machine running ssl vision
	 * @param printResults  if true, print offset and latency
	 * @return
	 */
	static public boolean setOffset(String ftpServerIP, boolean printResults) {
		try {
			NTPUDPClient client = new NTPUDPClient();
			client.open();			
			TimeInfo info = client.getTime(InetAddress.getByName(ftpServerIP));
			info.computeDetails(); // compute offset/delay if not already done
			Long offsetValue = info.getOffset();
			Long delayValue = info.getDelay();
			String delay = (delayValue == null) ? "N/A" : delayValue.toString();
			String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();
			
			if(offsetValue!=null) timeOffset = offsetValue / 1000.0;
			else {
				System.err.println("ERROR: Offset not available");
				return false;
			}
			
			if(printResults) 
				System.out.println(" Roundtrip delay(ms)=" + delay + ", clock offset(ms)=" + timeOffset); // offset in ms
			client.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		} 
		return true;
	}
	
	/***
	 * Default listener initializer
	 */
	static public void initListener() {
		initListener(VISION_ADDRESS,PORT);
	}
	
	
	/***
	 * Listener initializer with given multicast ip and port
	 * @param ip
	 * @param port
	 */
	static public void initListener(String ip, int port) {
		
		try {
			System.out.println("Initializing SSL Client...");
			group  = InetAddress.getByName(ip);
			socket = new MulticastSocket(port);
			socket.joinGroup(group);
			socket.setReceiveBufferSize(BUFFER_SIZE);
			System.out.println("Done initializing SSL Client...");
			
			
			detections.clear();
			
			setCloseListener(false);
			listenerThread = new Thread(() -> listen());
			listenerThread.start();
			
			
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
				
	}
	
	
	/***
	 * Function to be called to close listener
	 */
	static public void closeListener() {
		setCloseListener(true);
		try {
			listenerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/***
	 *
	 * @param robotId
	 * @return returns the last detection package for the given robot id
	 */
	static synchronized public Pair<SSL_DetectionRobot,Double> getDetection(String robotId) {
		return detections.get(robotId);
	}
	
	
	
	/***
	 * Private method to signal close listener
	 * @param value
	 */
	static synchronized void setCloseListener(boolean value) {
		closeListener = value;
	}
	
	
	
	/***
	 * Private method that adds a detection
	 * @param robotId		
	 * @param coord 
	 * @param time
	 */
	static synchronized void addDetection(String robotId, SSL_DetectionRobot detection_package,Double detectionTime) {
		detections.put(robotId,new Pair(detection_package,detectionTime));
	}
	
	
	
	
	/***
	 * Private method to read shared variable
	 * @return
	 */
	
	static synchronized Boolean getCloseListener() {
		return closeListener;
	}
	
	
	
	
	/***
	 * Private method that runs in a thread to listen for ssl packages
	 */
	static private void listen() {
		
		System.out.println("Listener Thread Started...");
		
		while(!getCloseListener()) {	
			try {
				
				//receive packet
				byte[] buf = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, BUFFER_SIZE);
				
				//System.out.println("Waiting for package...");
				socket.receive(packet);
				//System.out.println("Package received...");
				Double receiveTime = System.currentTimeMillis()/1000.0;
				byte[] data = new byte[packet.getLength()];
				System.arraycopy(buf, 0, data, 0, packet.getLength());
				
				
				SSL_WrapperPacket ssl_wrapper = SSL_WrapperPacket.parseFrom(data);
				
				//TODO process information according to camera id
				int cameraID = ssl_wrapper.getDetection().getCameraId();
				double captureTime = ssl_wrapper.getDetection().getTCapture() - timeOffset;
				
				
//				ssl_wrapper.getDetection().
				
				System.out.println("Times: "+ String.format("%.3f", receiveTime) + "\t" + String.format("%.3f", captureTime) );
				for(SSL_DetectionRobot r : ssl_wrapper.getDetection().getRobotsBlueList()){
					addDetection("b" + r.getRobotId(), r, receiveTime);
//					r.get
					//System.out.println("Found: " + r.getRobotId()  + " " + r.getX() + " " + r.getY());
				}
				
				for(SSL_DetectionRobot r : ssl_wrapper.getDetection().getRobotsYellowList()){
					addDetection("y" + r.getRobotId(), r, captureTime);						
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Listener Thread Closed");
		
		
	}
	
	

	
	
	
}


//import javax.vecmath.Point3f;
//

//

//
//public class VisionListener extends Thread {
//
//	private static VisionListener vl = null;
//	

//	
//	private final float fl = 2;
//

//	private RigidTransformation lastPosition;
//	private long lastPosTime;
//

//

//

//	
//	private synchronized Position getLastPosition(){
//		return lastPosition;
//	}
//
//	@Override
//	protected void finalize() throws Throwable {
//		super.finalize();
//		s.leaveGroup(group);
//		s.close();
//	}
//	
//	public synchronized boolean hasPosition(){
//		return lastPosTime > System.currentTimeMillis() - 300;
//	}
//
//	public synchronized Point3f getRobotPoint() {		
//		Position p = lastPosition;
//		return scale(new Point3f(p.getX(), p.getY(), 0));
//	}
//
//	private Point3f scale(Point3f p) {
//		float x = (-fl / (nwx - swx)) * p.x + (1 + (fl / (nwx - swx)) * swx);
//		float y = (fl / (ney - nwy)) * p.y + (-1 + (-fl / (ney - nwy)) * nwy);
//		x = Math.min(fl / 2, x);
//		x = Math.max(-fl / 2, x);
//		y = Math.min(fl / 2, y);
//		y = Math.max(-fl / 2, y);
//		// System.out.println(x + " " + y);
//		return new Point3f(x, y, 0);
//	}
//
//	public synchronized float getRobotOrientation() {
//		Position p = lastPosition;
//		// System.out.println(p.getOrient());
//		return p.getOrient();
//	}
//

//
//	public static VisionListener getVisionListener() {
//		if (vl == null)
//			vl  = new VisionListener();
//		
//		return vl;
//	}
//}
