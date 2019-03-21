package com.biocatch.auth.policy;
import static utils.BioCatchConsts.ACTION;
import static utils.BioCatchConsts.CUSTOMER_ID;
import static utils.BioCatchConsts.CUSTOMER_SESSION_ID;
import static utils.BioCatchConsts.INIT;
import static utils.BioCatchConsts.OK;

import javax.inject.Inject;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.SharedStateConstants;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.json.JSONObject;

import com.google.inject.assistedinject.Assisted;
import com.mashape.unirest.http.HttpResponse;

import utils.BioCatchConsts;
import utils.ExecuteGetScore;
import utils.ExecuteInit;

/**
 * 
 * @author Sacumen (www.sacumen.com)
 * 
 * It takes BioCatch end point URL and customer id from configuration parameter and execute BioCatch "init" API. 
 *
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class, configClass = BioCatchSessionNode.Config.class)
public class BioCatchSessionNode extends AbstractDecisionNode {
	
	private ExecuteInit init;
	private final Config config;
	
	public interface Config {

		// Getting BioCatch End Point URL
		@Attribute(order = 100, requiredValue = true)
		default String biocatchEndPoint() {
			return "";
		}
		
		// Getting customer ID
		@Attribute(order = 200, requiredValue = true)
		default String customerId() {
			return "";
		}

	}
	
	/**
	 * 
	 * @param config 
	 * 
	 * Injecting dependency
	 */
	@Inject
	public BioCatchSessionNode(@Assisted Config config,ExecuteInit init) {
		this.config = config;
		this.init = init;
	}
	

	/**
	 * It executes BioCatch "init" API.
	 */
	@Override
	public Action process(TreeContext context) throws NodeProcessException {
		String userName = context.sharedState.get(SharedStateConstants.USERNAME).asString();
		String sessionId = context.sharedState.get(CUSTOMER_SESSION_ID).asString();
		
		// Creating Json Request
		JSONObject json = new JSONObject();
        json.put(CUSTOMER_ID,config.customerId());
        json.put(ACTION,INIT);
        json.put(BioCatchConsts.UUID,userName);
        json.put(CUSTOMER_SESSION_ID, sessionId);
                
        // Executing BioCatch "init" API 
	    int status = init.init(json,config.biocatchEndPoint());
		if(status == OK) {
			return goTo(true).build();
		}
		else {
			return goTo(false).build();
		}
		}
}
