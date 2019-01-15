import java.io.IOException;

import ssl.SSLListener;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		System.out.println("Testing SSL vision client");
		
		SSLListener.initListener();
		
		try {
			while(System.in.read()==-1) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("key pressed, closing listener...");
		SSLListener.closeListener();
		
	}

}
