package de.uni_passau.fim.esl.crn_toolbox_center.modules;


import de.uni_passau.fim.esl.crn_toolbox_center.DataPacket;

public class SimpleGraphTCP extends TCPUDPModule {

	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public
	int getPort() {
		return port;
	}

	@Override
	public
	String getHost() {
		return host;
	}
	
	public DataPacket decodeData(String data) {
		return null;
	}

}


