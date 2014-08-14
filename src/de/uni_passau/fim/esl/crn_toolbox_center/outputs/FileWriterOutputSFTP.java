package de.uni_passau.fim.esl.crn_toolbox_center.outputs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.Log;
import de.uni_passau.fim.esl.crn_toolbox_center.modules.*;

import com.jcraft.jsch.*;


public class FileWriterOutputSFTP extends FileWriterOutput {
	
	private FileWriterSFTP sftpWriter;
	//private String startTime;
	private static String startTime;
	
	public static final String[] mSupportedModules = {"FileWriterSFTP"};

	public FileWriterOutputSFTP(Context context, List<Module> modulesToUse) {
		super(context, modulesToUse);		
		
		sftpWriter = (FileWriterSFTP) mModules.get(0);
		
		SimpleDateFormat dateTimeFormat =  new SimpleDateFormat("ddMMyyyy_HHmmss");
		startTime = dateTimeFormat.format(Calendar.getInstance().getTime());
	}

	@Override
	public void shutDown() {
		String password, user, host, destination;
		
		super.shutDown();
		
		try {	
			
			user = sftpWriter.getUser();
			host = sftpWriter.getHost();
			
			File sampleFile = new File( sftpWriter.getPathName());
			SimpleDateFormat dateTimeFormat =  new SimpleDateFormat("ddMMyyyy_HHmmss");
			String stopTime = dateTimeFormat.format(Calendar.getInstance().getTime());
			File transmitFile = new File( sftpWriter.getPathName() + "_" + startTime + "_" + stopTime);
						
			if( host != null && user != null && sampleFile.exists() )
			{
				sampleFile.renameTo(transmitFile);	
								
				JSch jsch = new JSch();
				
				Session session = jsch.getSession(sftpWriter.getUser(), sftpWriter.getHost(), sftpWriter.getPort());				
				
				password = sftpWriter.getPassword();
				if(password != null)
					session.setPassword(password);
				
				java.util.Properties config = new java.util.Properties(); 
		        config.put("StrictHostKeyChecking", "no"); 
		        session.setConfig(config); 
				
				session.connect();
				ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
				channel.connect();
								
				try
				{
					String home = channel.getHome();
					channel.cd(home);
				}
				catch(SftpException e)
				{
					
				}
							
				destination = sftpWriter.getDestinationFolder();
				if(destination != null)
				{
					try
					{
						channel.cd(destination);
					}catch(SftpException e)
					{
						channel.mkdir(destination);
						channel.cd(destination);
					}	
				}		
				 
				channel.put( transmitFile.getAbsolutePath(),  transmitFile.getName(), ChannelSftp.OVERWRITE);
					
				channel.disconnect();
				session.disconnect();
				
				transmitFile.delete();
			}
		}
		catch (SftpException e) {
			Log.e("sftp", e.getMessage());
		} catch (JSchException e) {
			Log.e("jsch", e.getMessage());			
		}
	}
}
