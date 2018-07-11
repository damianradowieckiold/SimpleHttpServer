package pl.itandmusic.simplehttpserver.request;

import java.io.IOException;
import java.net.Socket;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import pl.itandmusic.simplehttpserver.configuration.Configuration;
import pl.itandmusic.simplehttpserver.logger.Logger;
import pl.itandmusic.simplehttpserver.model.ServletContext;
import pl.itandmusic.simplehttpserver.model.HttpServletRequestImpl;
import pl.itandmusic.simplehttpserver.model.HttpServletResponseImpl;
import pl.itandmusic.simplehttpserver.model.RequestContent;
import pl.itandmusic.simplehttpserver.model.ServletConfig;
import pl.itandmusic.simplehttpserver.response.ResponseSendingService;
import pl.itandmusic.simplehttpserver.utils.URIResolver;
import pl.itandmusic.simplehttpserver.utils.URIUtils;

public class RequestThread implements Runnable {

	private final Logger logger = Logger.getLogger(RequestThread.class);
	private Socket socket;
	private HttpServletRequestImpl servletRequest;
	private HttpServletResponseImpl servletResponse;
	private RequestContentReader requestContentReader;
	private RequestContentConverter requestContentConverter;
	private ResponseSendingService responseSendingService;
	private ServletContext servletContext;

	public RequestThread(Socket socket) {
		this.socket = socket;
		this.requestContentReader = new RequestContentReader();
		this.requestContentConverter = new RequestContentConverter();
		this.responseSendingService = new ResponseSendingService();
	}

	@Override
	public void run() {

		RequestContent content = requestContentReader.read(socket);
		
		if (content.empty()) {
			return;
		}
		
		servletRequest = requestContentConverter.convert(content, socket);
		
		if (URIResolver.serverInfoRequest(servletRequest)) {
			responseSendingService.tryToLoadServerPage(socket);
		} 
		else if(URIResolver.defaultAppPageRequest(servletRequest)) {
			loadAppConfig();
			loadAppDefaultPage();
		}
		else if(URIResolver.anyAppRequest(servletRequest)){
			loadAppConfig();
			tryToServiceRequestUsingServlet();
		}
		else {
			responseSendingService.tryToSendPageNotFoundResponse(socket);
		}

		tryToCloseSocket(socket);

	}
	
	private void loadAppDefaultPage() {

		if(URIResolver.properDefaultAppPageRequest(servletRequest)) {
			responseSendingService.tryToLoadDefaultPage(socket, servletContext);
		}
		else {
			String URI = URIResolver.getRequsetURI(servletRequest);
			String correctedURI = URIUtils.correctUnproperDefaultPageURI(URI);
			responseSendingService.tryToSendRedirectResponse(socket,  correctedURI);
		}
	}
	
	private void tryToServiceRequestUsingServlet() {
		try {
			servletResponse = new HttpServletResponseImpl();
			
			Class<?> servletClass = loadClass(servletRequest);
			
			Servlet servlet = (Servlet) servletClass.newInstance();
			
			servlet.service(servletRequest, servletResponse);
			
			if (servletResponse.isRedirectResponse()) {
				responseSendingService.sendRedirectResponse(socket, servletResponse.getRedirectURL());
			} else {
				responseSendingService.sendOKResponse(socket, servletResponse);
			}

		} catch (InstantiationException | IllegalAccessException | ServletException | IOException e) {
			
			logger.error("Could not ...");
			logger.error("Error message: " + e.getMessage());
			
			responseSendingService.tryToSendInternalErrorResponse(socket);
		}
	}

	private void tryToCloseSocket(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			logger.warn("Conuld not close socket");
		}
	}

	

	private Class<?> loadClass(HttpServletRequestImpl request) {
		String requestURI = URIResolver.getRequsetURI(request);
		for(ServletConfig sc : servletContext.getServletConfigs()) {
			Class<?> clazz = sc.getServletMappings().get(requestURI);
			if(clazz != null) {
				return clazz;
			}
		}
		return null;
	}
	
	private void loadAppConfig() {
		String requestURI = URIResolver.getRequsetURI(servletRequest);
		for(String an : Configuration.applications.keySet()) {
			if(requestURI.contains(an)) {
				this.servletContext = Configuration.applications.get(an);
			}
		}
	}

}
