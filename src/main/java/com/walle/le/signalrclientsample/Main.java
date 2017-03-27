/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.walle.le.signalrclientsample;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author lhphuong86
 */
public class Main {

	public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("url", "http://XXX:XXX");
		jsonObject.addProperty("queryString", "id=XXX&passCode=XXX&clientVersion=1.0");
		jsonObject.addProperty("clientId", "XXX");
		jsonObject.addProperty("hubName", "XXX");
		jsonObject.addProperty("partnerId", "XXX");

		VcbConnection.init(jsonObject);
		JsonElement je = VcbConnection.getInstance().getHubProxy().invoke(JsonElement.class, "Request", jsonObject.get("partnerId").getAsString(), "1231243254",
				"this is mesage").get();
		System.out.println(je.getAsString());

	}
}
