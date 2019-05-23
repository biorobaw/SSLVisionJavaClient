import java.io.IOException;

import ssl.SSLListener;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		System.out.println("Testing SSL vision client");
		
		SSLListener.initListener();
		double fps = 30;
		Double oldTime =0.0;
		try {
			while(System.in.available()==0) {
				var d = SSLListener.getDetection("b1");
				if (d!=null) {
					var dTime   = d.getValue();
					var deltaT = dTime - oldTime;
					if(deltaT == 0 ) continue;
					oldTime = dTime;
					fps = 0.9*fps + 0.1 *1000/deltaT;
					
					var dRobot = d.getKey();
					
					System.out.println("Detected: " + dRobot.getRobotId());
					System.out.println("Pos,tita: (" + dRobot.getX() + "," + dRobot.getY() + "," + dRobot.getOrientation() +")" );
					System.out.println("TIME:     " + dTime );
					System.out.println("deltaT:   " + deltaT);
					System.out.println("fps:      " + fps);
					System.out.println();
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				
			};
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("key pressed, closing listener...");
		SSLListener.closeListener();
		
	}

}
