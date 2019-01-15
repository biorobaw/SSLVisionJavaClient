package ssl;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;

import ssl.MessagesRobocupSslDetection.SSL_DetectionRobot;
import ssl.MessagesRobocupSslWrapper.SSL_WrapperPacket;

import com.vividsolutions.jts.geom.Coordinate;

import javafx.util.Pair;

//import edu.usf.ratsim.robot.naorobot.protobuf.MessagesRobocupSslDetection.SSL_DetectionRobot;
//import edu.usf.ratsim.robot.naorobot.protobuf.MessagesRobocupSslWrapper.SSL_WrapperPacket;
//import edu.usf.ratsim.support.Position;


public class SSLListener {
	
	static InetAddress 	   group;
	static MulticastSocket socket;
	
	static final String VISION_ADDRESS = "224.5.23.2";
	static final int PORT = 10006;
	static final int BUFFER_SIZE = 4096;
	
	static HashMap<String, Pair<SSL_DetectionRobot,Long>> detections = new HashMap();
	
	static Boolean closeListener = false;
	static Thread listenerThread = null;
	
	static synchronized void addDetection(String id, SSL_DetectionRobot coord,Long time) {
		detections.put(id,new Pair(coord,time));
	}
	
	static synchronized public Pair<SSL_DetectionRobot,Long> getDetection(String id) {
		return detections.get(id);
	}
	
	
	static synchronized void setCloseListener(boolean value) {
		closeListener = value;
	}
	
	static synchronized Boolean getCloseListener() {
		return closeListener;
	}
	
	
	
	static public void initListener() {
		
		try {
			System.out.println("Initializing SSL Client...");
			group  = InetAddress.getByName(VISION_ADDRESS);
			socket = new MulticastSocket(PORT);
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
	
	static public void closeListener() {
		setCloseListener(true);
		try {
			listenerThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static public void listen() {
		
		System.out.println("Listener Thread Started...");
		
		while(!getCloseListener()) {	
			try {
				
				//receive packet
				byte[] buf = new byte[BUFFER_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, BUFFER_SIZE);
				
				System.out.println("Waiting for package...");
				socket.receive(packet);
				System.out.println("Package received...");
				Long receiveTime = System.currentTimeMillis();
				byte[] data = new byte[packet.getLength()];
				System.arraycopy(buf, 0, data, 0, packet.getLength());
				
				
				SSL_WrapperPacket ssl_wrapper = SSL_WrapperPacket.parseFrom(data);
				
				
				for(SSL_DetectionRobot r : ssl_wrapper.getDetection().getRobotsBlueList()){
					addDetection("b" + r.getRobotId(), r, receiveTime);			
					//System.out.println("Found: " + r.getRobotId());
				}
				
				for(SSL_DetectionRobot r : ssl_wrapper.getDetection().getRobotsYellowList()){
					addDetection("y" + r.getRobotId(), r, receiveTime);						
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		System.out.println("Listener Thread Closed");
		
		
	}
	
	
	
//	public static void main(String[] args) {
//	
//	// VisionListener v = new VisionListener();
//	// while (true){
//	// System.out.println(v.getRobotPoint());
//	// try {
//	// Thread.sleep(100);
//	// } catch (InterruptedException e) {
//	// // TODO Auto-generated catch block
//	// e.printStackTrace();
//	// }
//	// }
//	Position p = null;
//	try {
//		VisionListener v = new VisionListener();
//		Thread.sleep(10000);
//		p = v.getLastPosition();
//		System.out.println("private final float nex = " + p.getX() + "f;");
//		System.out.println("private final float ney = " + p.getY() + "f;");
//		Thread.sleep(10000);
//		p = v.getLastPosition();
//		System.out.println("private final float sex = " + p.getX() + "f;");
//		System.out.println("private final float sey = " + p.getY() + "f;");
//		Thread.sleep(10000);
//		p = v.getLastPosition();
//		System.out.println("private final float swx = " + p.getX() + "f;");
//		System.out.println("private final float swy = " + p.getY() + "f;");
//		Thread.sleep(10000);
//		p = v.getLastPosition();
//		System.out.println("private final float nwx = " + p.getX() + "f;");
//		System.out.println("private final float nwy = " + p.getY() + "f;");
//	} catch (InterruptedException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//}
	
	
	
}


////North east
////(414, 1270)
////
////South east
////(2740, 1309)	
////
////South west
////(2719, -1014)
////
////North west
////(397, -1020)

//private final float nex = 272.33234f;
//private final float ney = 1152.6455f;
//private final float sex = 2602.887f;
//private final float sey = 1195.9545f;
//private final float swx = 2588.3923f;
//private final float swy = -1225.2101f;
//private final float nwx = 223.70718f;
//private final float nwy = -1251.4977f;




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
