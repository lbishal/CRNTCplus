package de.uni_passau.fim.esl.crn_toolbox_center.modules;

public abstract class TCPUDPModule extends Module{
	protected int port;
	protected String host;

	public int getPort() {
		return port;
	}
	
	public String getHost() {
		return host;
	}
	
}
