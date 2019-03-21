package com.biocatch.auth.policy;

import static org.mockito.MockitoAnnotations.initMocks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.junit.Assert;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.sun.identity.authentication.internal.server.AuthSPrincipal;
import com.sun.identity.entitlement.ConditionDecision;
import com.sun.identity.entitlement.EntitlementException;

import utils.ExecuteGetScore;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ExecuteGetScore.class)
public class BioCatchConditionTypeTest {
	
	 @Mock
	 ExecuteGetScore action;
	 
	 private static BioCatchConditionType biocatchConditionType;

	 public static final String SCORE_FIELD = "score";
	 public static final String BIO_CATCH_END_POINT_URL_FIELD = "bioCatchEndPointUrl";
	 public static final String CUSTOMER_ID_FIELD = "customerId";
	 public static final String ADVICE_FIELD = "advice";
	 public static final String LEVEL_FIELD = "level";
	 public static final String CACHE_EXPIRATION_TIME_FIELD = "cacheExpirationTime";


	    @BeforeMethod
	    public void init() {
	    	initMocks(this);
	    	}

	    @Test
	    public void setStateTest() {
	    	biocatchConditionType = new BioCatchConditionType();

	        String json = loadJsonFromResource("happyState.json");
	        biocatchConditionType.setState(json);
	        try {
	            JSONObject jsonObject = new JSONObject(json);
	            Assert.assertEquals(jsonObject.getInt(SCORE_FIELD),biocatchConditionType.getScore());
	            Assert.assertEquals(jsonObject.getString(BIO_CATCH_END_POINT_URL_FIELD),biocatchConditionType.getBioCatchEndPointUrl());
	            Assert.assertEquals(jsonObject.getString(CUSTOMER_ID_FIELD),biocatchConditionType.getCustomerId());
	            Assert.assertEquals(jsonObject.getString(ADVICE_FIELD),biocatchConditionType.getAdvice());
	            Assert.assertEquals(jsonObject.getInt(CACHE_EXPIRATION_TIME_FIELD),biocatchConditionType.getCacheExpirationTime());
	            Assert.assertEquals(jsonObject.getString(LEVEL_FIELD),biocatchConditionType.getLevel());

	        } catch (JSONException e) {
	            e.printStackTrace();
	            Assert.fail("Something went wrong...");
	        }
	    }

	    @Test
	    public void getStateTest() {
	    	biocatchConditionType = new BioCatchConditionType();
	        String expectedState = loadJsonFromResource("happyState.json");
	        biocatchConditionType.setState(expectedState);
	        String state = biocatchConditionType.getState();
	        try {
	            JSONObject expectedStateJson = new JSONObject(expectedState);
	            JSONObject actualSteteJson = new JSONObject(state);

	            Assert.assertEquals(expectedStateJson.getInt(SCORE_FIELD), actualSteteJson.getInt(SCORE_FIELD));
	            Assert.assertEquals(expectedStateJson.getString(CUSTOMER_ID_FIELD), actualSteteJson.getString(CUSTOMER_ID_FIELD));
	            Assert.assertEquals(expectedStateJson.getString(LEVEL_FIELD), actualSteteJson.getString(LEVEL_FIELD));
	            Assert.assertEquals(expectedStateJson.getString(ADVICE_FIELD), actualSteteJson.getString(ADVICE_FIELD));
	            Assert.assertEquals(expectedStateJson.getInt(CACHE_EXPIRATION_TIME_FIELD), actualSteteJson.getInt(CACHE_EXPIRATION_TIME_FIELD));
	            Assert.assertEquals(expectedStateJson.getString(BIO_CATCH_END_POINT_URL_FIELD), actualSteteJson.getString(BIO_CATCH_END_POINT_URL_FIELD));

	        } catch (JSONException e) {
	            e.printStackTrace();
	            Assert.fail("Something went wrong...");
	        }

	    }

	    @Test(expectedExceptions = EntitlementException.class)
	    public void validateTest_fail() throws EntitlementException {
	    	biocatchConditionType = new BioCatchConditionType();

	        String state = loadJsonFromResource("happyState.json");
	        biocatchConditionType.setState(state);
	        biocatchConditionType.setScore(-5);
	        biocatchConditionType.validate();
	    }

	    @Test
	    public void evaluateTest() throws Exception {
			
            Set<Principal> userPrincipals = new HashSet<Principal>(2);
            userPrincipals.add(new AuthSPrincipal("user1=testUser,user1=testUser"));
            Set privateCred = new HashSet();
            
            Subject subject = new Subject(false, userPrincipals, new HashSet(),
                    privateCred);

            Map<String, Set<String>> map = new HashMap<String, Set<String>>();
	    	Set<String> set1 = new HashSet<String>();
	    	set1.add("session123");
	    	map.put("customerSessionID", set1);
	    	
	    	HttpResponseFactory factory = new DefaultHttpResponseFactory();
	    	org.apache.http.HttpResponse response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
	    	JSONObject json = new JSONObject();
	    	json.put("score",10);
	    	json.put("bcStatus","tested");
	    	response.setEntity(new StringEntity(json.toString()));
	    	com.mashape.unirest.http.HttpResponse<JsonNode> httpResponse = new HttpResponse<JsonNode>(response, JsonNode.class);
		    
		    Whitebox.setInternalState(ExecuteGetScore.class, "single_instance", action);
		    Mockito.when(action.getEntry(ArgumentMatchers.any())).thenReturn(httpResponse);


	        // Auth
	    	biocatchConditionType = new BioCatchConditionType();
	    	String expectedState = loadJsonFromResource("happyState.json");
	        biocatchConditionType.setState(expectedState);
	        ConditionDecision evaluated = biocatchConditionType.evaluate("",subject, null,map);
	        Assert.assertEquals(Boolean.TRUE, evaluated.isSatisfied());
	    }

	    @Test
	    public void evaluateTest_fail() throws Exception {
	    	 Set<Principal> userPrincipals = new HashSet<Principal>(2);
	            userPrincipals.add(new AuthSPrincipal("user1=testUser,user1=testUser"));
	            Set privateCred = new HashSet();
	            
	            Subject subject = new Subject(false, userPrincipals, new HashSet(),
	                    privateCred);

	            Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		    	Set<String> set1 = new HashSet<String>();
		    	set1.add("session123");
		    	map.put("customerSessionID", set1);
		    	
		    	HttpResponseFactory factory = new DefaultHttpResponseFactory();
		    	org.apache.http.HttpResponse response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
		    	JSONObject json = new JSONObject();
		    	json.put("score",400);
		    	json.put("bcStatus","tested");
		    	response.setEntity(new StringEntity(json.toString()));
		    	com.mashape.unirest.http.HttpResponse<JsonNode> httpResponse = new HttpResponse<JsonNode>(response, JsonNode.class);
			    
			    Whitebox.setInternalState(ExecuteGetScore.class, "single_instance", action);
			    Mockito.when(action.getEntry(ArgumentMatchers.any())).thenReturn(httpResponse);


		        // Auth
		    	biocatchConditionType = new BioCatchConditionType();
		    	String expectedState = loadJsonFromResource("happyState.json");
		        biocatchConditionType.setState(expectedState);
		        ConditionDecision evaluated = biocatchConditionType.evaluate("",subject, null,map);
		        Assert.assertEquals(Boolean.FALSE, evaluated.isSatisfied());
	    }

	    private String loadJsonFromResource(String path) {
	        InputStream inputStream = this.getClass().getResourceAsStream("/" + path);
	        if(inputStream == null) {
	            return "";
	        }
	        try {
	            BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	            StringBuilder sb = new StringBuilder();

	            String inputStr;
	            while ((inputStr = streamReader.readLine()) != null) {
	                sb.append(inputStr);
	            }
	            streamReader.close();
	            return sb.toString();
	        } catch (Exception e) {
	            e.printStackTrace();
	            return "";
	        }
	    }
}
