package pl.itandmusic.simplehttpserver.configuration.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import pl.itandmusic.simplehttpserver.configuration.Configuration;
import pl.itandmusic.simplehttpserver.logger.LogLevel;
import pl.itandmusic.simplehttpserver.logger.Logger;

public class ServerConfigurationLoader {

	public static void load() throws JAXBException, FileNotFoundException {
		
		Logger.log("Server configuration loading.", LogLevel.INFO);
		
		File serverXml = new File("/home/damian/eclipse-workspace/Other/SimpleHttpServer/config/server.xml");
		if (serverXml.exists()) {
			JAXBContext context = JAXBContext.newInstance(ServerConfig.class);
			Unmarshaller um = context.createUnmarshaller();
			ServerConfig serverConfig = (ServerConfig) um.unmarshal(new FileReader(serverXml));
			Configuration.port = serverConfig.getPort();
			Configuration.appDirectory = serverConfig.getAppDirectory();
		} else {
			throw new FileNotFoundException("Cannot find server.xml file");
		}
		
		Logger.log("Server configuration loaded.", LogLevel.INFO);
		
	}
}
