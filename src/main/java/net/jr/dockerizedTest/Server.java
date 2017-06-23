package net.jr.dockerizedTest;

public interface Server {

	public void start();
	
	public boolean isRunning();
	
	public int getPort();
	
	public void stop();
}
