package com.biocatch.auth.policy;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.forgerock.json.JsonValue.object;
import static org.forgerock.json.test.assertj.AssertJJsonValueAssert.assertThat;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static utils.BioCatchConsts.CUSTOMER_SESSION_ID;
import utils.ExecuteGetScore;
import utils.ExecuteInit;

@PrepareForTest({ExecuteGetScore.class})
public class BioCatchSessionNodeTest {
	
	 @Mock
	 ExecuteInit action;
	 
	 @Mock
	 BioCatchSessionNode.Config config;
	 
	 @InjectMocks
	 BioCatchSessionNode node;

	@BeforeMethod
	public void before() {
  		initMocks(this);
	}

	@Test
	public void testProcessWithTrueOutcome() throws NodeProcessException {
	    Mockito.when(action.init(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(200);
		
        TreeContext context = getTreeContext(new HashMap<>());
        
		context.sharedState.put(USERNAME,"test123");
		context.sharedState.put(CUSTOMER_SESSION_ID,"SYMC87283752");
				
		
		Action action = node.process(context);
		
		//THEN
		assertThat(action.callbacks).isEmpty();
	    assertThat(context.sharedState).isObject().contains(entry(USERNAME, "test123"),entry(CUSTOMER_SESSION_ID, "SYMC87283752"));
	    assertThat(action.outcome).isEqualTo("true");
	}
	
	
	@Test
	public void testProcessWithFalseOutcome() throws NodeProcessException {

	    Mockito.when(action.init(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(401);
		
        TreeContext context = getTreeContext(new HashMap<>());
        
		context.sharedState.put(USERNAME,"test123");
		context.sharedState.put(CUSTOMER_SESSION_ID,"SYMC87283752");
				
		// WHEN
		Action action = node.process(context);
		
		//THEN
		assertThat(action.callbacks).isEmpty();
	    assertThat(context.sharedState).isObject().contains(entry(USERNAME, "test123"),entry(CUSTOMER_SESSION_ID, "SYMC87283752"));
	    assertThat(action.outcome).isEqualTo("false");
	}
	
	private TreeContext getTreeContext(Map<String, String[]> parameters) {
		return new TreeContext(JsonValue.json(object(1)),
				new ExternalRequestContext.Builder().parameters(parameters).build(), emptyList());
	}
}
