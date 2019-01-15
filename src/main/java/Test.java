import java.io.IOException;

import ssl.SSLListener;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		System.out.println("Testing SSL vision client");
		
		SSLListener.initListener();
		double fps = 30;
		Long oldTime =0L;
		try {
			while(System.in.read()==-1) {
				var d = SSLListener.getDetection("b1");
				if (d!=null) {
					var dRobot = d.getKey();
					var dTime   = d.getValue();
					var deltaT = dTime = oldTime;
					oldTime = dTime;
					fps = 0.9*fps + 0.1 /deltaT;
					System.out.println("Detected: " + dRobot.getRobotId());
					System.out.println("Pos,tita: (" + dRobot.getX() + "," + dRobot.getY() + "," + dRobot.getOrientation() +")" );
					System.out.println("TIME:     " + dTime );
					System.out.println("deltaT:   " + deltaT);
					System.out.println("fps:      " + fps);
					System.out.println();
					
					
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
