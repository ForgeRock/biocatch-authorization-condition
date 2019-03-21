package utils;

import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.json.JSONObject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import com.sun.identity.shared.debug.Debug;
import static utils.BioCatchConsts.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Sacumen(www.sacumen.com)
 * 
 * This class actually execute BioCatch APIs and return response.
 * 
 * It also keeps cache of response of getScore call for configured time.
 *
 */
public class ExecuteGetScore {
	public LoadingCache<String,HttpResponse<JsonNode>> cache;
	private String endPoint;
    private JSONObject json;
	private final Debug debug = Debug.getInstance("BioCatch");



	// static variable single_instance of type Singleton
	private static ExecuteGetScore single_instance = null;

	// static method to create instance of Singleton class
	public static ExecuteGetScore getInstance(int cacheExpirationTime) {
		if (single_instance == null)
			single_instance = new ExecuteGetScore(cacheExpirationTime);

		return single_instance;
	}

	//Loads cache
	private ExecuteGetScore(int cacheExpirationTime) {
		cache = CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(cacheExpirationTime,TimeUnit.SECONDS)
				.build(new CacheLoader<String, HttpResponse<JsonNode>>() {

					@Override
					public HttpResponse<JsonNode> load(String json) throws Exception {
						return addCache(json);
					}

				});
	}

	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	
	public void setJsonRequest(JSONObject json) {
		 this.json=json;
	 }

	/**
	 * 
	 * @param key Key which stores cache.
	 * @return HttpResponse
	 * @throws NodeProcessException
	 * 
	 *  Executing BioCatch "getScore" API
	 */
	private HttpResponse<JsonNode> getScore(String key) throws NodeProcessException {
		debug.message("inside get score");
		debug.message("json is " + json);

		HttpResponse<JsonNode> response;
		try {
			response = executeRequest(json, endPoint).asJson();
		} catch (UnirestException e) {
			throw new NodeProcessException(e.getLocalizedMessage());
		}

		debug.message("rsponse is " + response.getBody().toString());
		return response;
	}

	/**
	 * 
	 * @param arg Key to store cache
	 * @return HttpResponse from cache
	 * @throws NodeProcessException
	 * 
	 * It adds cache in memory.
	 */
	private HttpResponse<JsonNode> addCache(String arg) throws NodeProcessException {
		return getScore(arg);

	}

	/**
	 * 
	 * @param arg Key to store cache
	 * @return HttpResponse from cache
	 * @throws ExecutionException
	 * 
	 * Get HttpResponse from cache.
	 */
	public HttpResponse<JsonNode> getEntry(String arg) throws ExecutionException {
		debug.message(String.valueOf(cache.size()));
		debug.message("cache is "+cache.getIfPresent(arg));
		return cache.get(arg);
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
