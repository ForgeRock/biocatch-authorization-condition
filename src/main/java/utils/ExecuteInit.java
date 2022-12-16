package utils;

import static utils.BioCatchConsts.APPLICATION_JSON;
import static utils.BioCatchConsts.NO_CACHE;

import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;

public class ExecuteInit {


	/**
	 * 
	 * @return HttpResponse
	 * @throws NodeProcessException
	 * 
	 * Executing BioCatch "init" API
	 */
	public int init(JSONObject json,String endPoint) throws NodeProcessException {
		HttpResponse<String> response = null;
		try {
			response = executeRequest(json, endPoint).asString();
		} catch (UnirestException e) {
			throw new NodeProcessException(e.getLocalizedMessage());
		}
		if(response == null) {
			throw new NodeProcessException("Not able to get response from BioCatch init API");
		}
		return response.getStatus();
	}
	
	/**
	 * 
	 * @param json Json Payload
	 * @param endPoint Endpoint URL
	 * @return RequestBodyEntity
	 * 
	 * Execute request
	 */
	public RequestBodyEntity executeRequest(JSONObject json, String endPoint) {
		RequestBodyEntity response;
		response = Unirest.post(endPoint).header("content-type", APPLICATION_JSON).header("cache-control", NO_CACHE)
				.body(json);
		return response;
	}
}
