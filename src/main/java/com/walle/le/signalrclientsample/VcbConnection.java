/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.walle.le.signalrclientsample;

import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.hubs.HubConnection;
import microsoft.aspnet.signalr.client.hubs.HubProxy;
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport;

/**
 *
 * @author vu
 */
public class VcbConnection {

	private static VcbConnection connection;

	private HubConnection hubConnection;
	private HubProxy hubProxy;

	private VcbConnection(JsonObject config) throws InterruptedException, ExecutionException, TimeoutException {
		createConnection(config);
		startPing();
	}

	public static void init(JsonObject config) throws ExecutionException, InterruptedException, TimeoutException {
		connection = new VcbConnection(config);
	}

	public static VcbConnection getInstance() {
		return connection;
	}

	public HubConnection getHubConnection() {
		return hubConnection;
	}

	public HubProxy getHubProxy() {
		return hubProxy;
	}

	private void createConnection(JsonObject config) throws InterruptedException, ExecutionException, TimeoutException {
		Logger logger = (String message, LogLevel level) -> {

			switch (level) {
				case Information:
				case Critical:
					System.out.println(message);
					break;
				default:
					System.out.println(message);

			}
		};

		hubConnection = new HubConnection(config.get("url").getAsString(), config.get("queryString").getAsString(), true, logger);
		hubConnection.closed(() -> {
			System.out.println("Connection is disconnected");

		});
		hubConnection.connected(() -> {
			System.out.println("Connection is connected");

		});
		String clientId = config.get("clientId").getAsString();
		hubProxy = hubConnection.createHubProxy(config.get("hubName").getAsString());
		hubProxy.on("Response", (String cliendId, String requestId, String requestObject) -> {
			System.out.println("RECEIVED DATA FROM VCB: clientId[" + cliendId + "], data:[" + requestObject + "]");

			if (cliendId.equals(clientId)) {
				String replyString = processReceiveRequest(requestId, requestObject);
				hubProxy.invoke("Response", config.get("partnerId").getAsString(), requestId, replyString);
			}
			else {
				System.out.println(" ClientId[" + cliendId + "] is NOT RIGHT [" + clientId + "]. BYPBASS!!");
			}
		}, String.class, String.class, String.class);

		if (hubConnection.getState() != ConnectionState.Connected) {
			hubConnection.start(new ServerSentEventsTransport(logger)).get(1, TimeUnit.MINUTES);
		}
	}

	private String processReceiveRequest(String requestId, String reqObject) {

		System.out.println("[RECEIVE REQUEST] : *******************************************************");
		System.out.println("[RECEIVE REQUEST] : " + reqObject);
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("Data", "LE HOAI PHUONG");
		jsonObject.addProperty("MessageType", "check_active");
		return jsonObject.toString();

	}

	private void startPing() {
		Timer timer = new Timer("PING", true);
		timer.scheduleAtFixedRate(new PingTimer(), 1000 * 60, 1000 * 60);
	}

	private class PingTimer extends TimerTask {

		@Override
		public void run() {
			try {
				System.out.println(new SimpleDateFormat("dd/MM/yyyy kk:mm:ss").format(new Date(System.currentTimeMillis())) + ": Connection status: " + hubConnection.getState());
				if (hubConnection.getState() == ConnectionState.Disconnected) {
					Logger logger = (String message, LogLevel level) -> {
						switch (level) {
							case Information:
							case Critical:
								System.out.println(message);
								break;
							default:
								System.out.println(message);

						}
					};
					hubConnection.start(new ServerSentEventsTransport(logger)).get(1, TimeUnit.MINUTES);
				}
				else {

					hubProxy.invoke("Ping", "XXX").get();

				}

			}
			catch (InterruptedException | ExecutionException | TimeoutException ex) {
				System.err.println(new SimpleDateFormat("dd/MM/yyyy kk:mm:ss").format(new Date(System.currentTimeMillis())) + "Ping error!!!  " + ": Connection status: " + hubConnection.getState());
			}
		}
	}

}
