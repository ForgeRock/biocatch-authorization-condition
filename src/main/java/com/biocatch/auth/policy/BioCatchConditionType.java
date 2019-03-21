package com.biocatch.auth.policy;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.sun.identity.entitlement.ConditionDecision;
import com.sun.identity.entitlement.EntitlementConditionAdaptor;
import com.sun.identity.entitlement.EntitlementException;
import com.sun.identity.entitlement.opensso.SubjectUtils;
import com.sun.identity.shared.debug.Debug;
import utils.ExecuteGetScore;
import org.json.JSONException;
import org.json.JSONObject;
import javax.security.auth.Subject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static utils.BioCatchConsts.*;

/**
 * 
 * @author Sacumen (www.sacumen.com)
 * 
 * This class evaluate BioCatch Authorization condition.
 */
public class BioCatchConditionType extends EntitlementConditionAdaptor {
	
	private final Debug debug = Debug.getInstance("BioCatch");

	public static final String SCORE_FIELD = "score";
	private int score;
	public static final String BIO_CATCH_END_POINT_URL_FIELD = "bioCatchEndPointUrl";
	private String bioCatchEndPointUrl;
	public static final String CUSTOMER_ID_FIELD = "customerId";
	private String customerId;
	public static final String ADVICE_FIELD = "advice";
	private String advice;
	public static final String LEVEL_FIELD = "level";
	private String level;
	public static final String CACHE_EXPIRATION_TIME_FIELD = "cacheExpirationTime";
	private int cacheExpirationTime;

	
	/**
	 * Set json fields, Which is coming from policy editor.
	 */
	public void setState(String state) {
		try {
			JSONObject json = new JSONObject(state);

			if (json.has(SCORE_FIELD)) {
				setScore(json.getInt(SCORE_FIELD));
			}
			if (json.has(BIO_CATCH_END_POINT_URL_FIELD)) {
				setBioCatchEndPointUrl(json.getString(BIO_CATCH_END_POINT_URL_FIELD));
			}
			if (json.has(CUSTOMER_ID_FIELD)) {
				setCustomerId(json.getString(CUSTOMER_ID_FIELD));
			}
			if (json.has(ADVICE_FIELD)) {
				setAdvice(json.getString(ADVICE_FIELD));
			}
			if (json.has(LEVEL_FIELD)) {
				setLevel(json.getString(LEVEL_FIELD));
			}
			if (json.has(CACHE_EXPIRATION_TIME_FIELD)) {
				setCacheExpirationTime(json.getInt(CACHE_EXPIRATION_TIME_FIELD));
			}
			this.validate();
		} catch (JSONException e) {
			this.debug.error("Failed to set state", e);
		} catch (EntitlementException e) {
			this.debug.error("Failed to validate state", e);
		}
	}

	/**
	 * Get json fields values, Which is sent by user using policy editor.
	 */
	public String getState() {
		try {
			JSONObject json = new JSONObject();
			json.put(SCORE_FIELD, getScore());
			json.put(BIO_CATCH_END_POINT_URL_FIELD, getBioCatchEndPointUrl());
			json.put(CUSTOMER_ID_FIELD, getCustomerId());
			json.put(ADVICE_FIELD, getAdvice());
			json.put(LEVEL_FIELD, getLevel());
			json.put(CACHE_EXPIRATION_TIME_FIELD, getCacheExpirationTime());
			return json.toString();
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Validates the json.
	 */
	public void validate() throws EntitlementException {
		if (this.score < 0) {
			throw new EntitlementException(EntitlementException.INVALID_PROPERTY_VALUE, SCORE_FIELD);
		}
		if (this.bioCatchEndPointUrl.isEmpty()) {
			throw new EntitlementException(EntitlementException.INVALID_PROPERTY_VALUE, BIO_CATCH_END_POINT_URL_FIELD);
		}
		if (this.customerId.isEmpty()) {
			throw new EntitlementException(EntitlementException.INVALID_PROPERTY_VALUE, CUSTOMER_ID_FIELD);
		}
		if (this.level.isEmpty()) {
			throw new EntitlementException(EntitlementException.INVALID_PROPERTY_VALUE, LEVEL_FIELD);
		}
		if (this.cacheExpirationTime < 0) {
			throw new EntitlementException(EntitlementException.INVALID_PROPERTY_VALUE,CACHE_EXPIRATION_TIME_FIELD);
		}
		
	}

	/**
	 * Evaluates the policy.
	 * It takes customerId, Advice, score, end point and level values from the user and execute BioCatch getScore API.
	 * 
	 * Based on configured score and decision level, it will send a action with get & post allowed for given resource.
	 */
	public ConditionDecision evaluate(String s, Subject subject, String s1, Map<String, Set<String>> map){
		ExecuteGetScore action = ExecuteGetScore.getInstance(getCacheExpirationTime());

		
		// Customer Session id Check
		if (!map.containsKey(CUSTOMER_SESSION_ID)) {
			return ConditionDecision.newBuilder(false).setAdvice(
					Collections.singletonMap(BIOCATCH_ADVICE_KEY, Collections.singleton(CustomerSessionId_IS_NOT_PRESENT)))
					.build();
		}
	
		String level = getLevel();
		double configuredScore = getScore();
		String uuid = SubjectUtils.getPrincipalId(subject).split(",")[0].split("=")[1];
		String csid = map.get(CUSTOMER_SESSION_ID).iterator().next();
		int actualScore;

		if (debug.messageEnabled()) {
			debug.message("request URL is " + getBioCatchEndPointUrl());
			debug.message("customer id is " + getCustomerId());
			debug.message("configured score is " + configuredScore);
			debug.message("configured level is " + level);
		}

        // Creating JSON Request
		JsonNode response;
		JSONObject json = new JSONObject();
		for (String param : map.keySet()) {
			json.put(param, map.get(param).iterator().next());
		}

		json.put(CUSTOMER_ID, getCustomerId());
		json.put(ACTION, GET_SCORE);
		json.put(UUID, uuid);

		action.setEndPoint(getBioCatchEndPointUrl());
		action.setJsonRequest(json);

		// Executing BioCatch getScore API
		try {
			HttpResponse<JsonNode> rawResponse = action.getEntry(uuid + ":" + csid);
			if(rawResponse == null) {
				return ConditionDecision.newBuilder(false).setAdvice(
						Collections.singletonMap(BIOCATCH_ADVICE_KEY, Collections.singleton(NULL_RESPONSE)))
						.build();
			}
			response = rawResponse.getBody();
			String bc_status = response.getObject().get(POLICY_STATE).toString();
			debug.message("bc_status is "+bc_status);
			if (!bc_status.equalsIgnoreCase(GET_SCORE_SUCCESS_STATUS)) {
				action.cache.invalidateAll();
				return ConditionDecision.newBuilder(false).setAdvice(Collections.singletonMap(BIOCATCH_ADVICE_KEY,
						Collections.singleton(POLICY_STATE+":"+ bc_status))).build();
			}
			actualScore = (int) response.getObject().get(SCORE);
		} catch (ExecutionException e) {
			return ConditionDecision.newFailureBuilder().build();
		}

		debug.message("actual score is " + actualScore);

		//Making decision based on configured score, configured Authentication level and actual score from getScore API.
		switch (level) {
		case ("<"):
			if (actualScore < configuredScore) {
				debug.message("actualScore is less than configuredScore");
				return ConditionDecision.newBuilder(true).build();
			} else {
				return getFalseDecesionWithAdvice();
			}

		case (">"):
			if (actualScore > configuredScore) {
				debug.message("actualScore is greater than configuredScore");
				return ConditionDecision.newBuilder(true).build();
			} else {
				return getFalseDecesionWithAdvice();
			}
		case ("="):
			if (actualScore == configuredScore) {
				debug.message("actualScore is equal to configuredScore");
				return ConditionDecision.newBuilder(true).build();
			} else {
				return getFalseDecesionWithAdvice();
			}

		default:
			return ConditionDecision.newBuilder(false).setAdvice(Collections.singletonMap(BIOCATCH_ADVICE_KEY,
					Collections.singleton(LEVEL_IS_NOT_SET_PROPERLY))).build();
		}

	}

	// Sending empty action with advice if it is configured.
	private ConditionDecision getFalseDecesionWithAdvice() {
		String advice = getAdvice();
		if (!advice.isEmpty()) {
			debug.message("setting advice");
			return ConditionDecision.newBuilder(false)
					.setAdvice(Collections.singletonMap(BIOCATCH_ADVICE_KEY, Collections.singleton(advice))).build();
		} else {
			debug.message("advice is empty...");
			return ConditionDecision.newBuilder(false).build();
		}
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getBioCatchEndPointUrl() {
		return bioCatchEndPointUrl;
	}

	public void setBioCatchEndPointUrl(String bioCatchEndPointUrl) {
		this.bioCatchEndPointUrl = bioCatchEndPointUrl;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getAdvice() {
		return advice;
	}

	public void setAdvice(String advice) {
		this.advice = advice;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}
	
	public int getCacheExpirationTime() {
		return cacheExpirationTime;
	}

	public void setCacheExpirationTime(int cacheExpirationTime) {
		this.cacheExpirationTime = cacheExpirationTime;
	}

}
