package de.uni_passau.fim.esl.crn_toolbox_center.modules;

public class FileWriterSFTP extends FileWriter {
	private String host;
	private String user;
	private String password;
	private int port;
	private String destinationFolder;
	 
	
	public String getHost() {
		return host;
	}
	public String getUser() {
		return user;
	}
	public String getPassword() {
		return password;
	}
	public int getPort() {
		if( port == 0)
		{
			//Default port for SFTP is 22
			return 22;
		}
		else
			return port;
	}
	
	public String getDestinationFolder()
	{
		return destinationFolder;		
	}	
}
