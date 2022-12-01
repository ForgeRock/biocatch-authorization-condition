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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.inject.assistedinject.Assisted;
import com.mashape.unirest.http.HttpResponse;
import org.forgerock.util.i18n.PreferredLocales;
import java.util.List;
import org.forgerock.json.JsonValue;
import utils.BioCatchConsts;
import utils.ExecuteGetScore;
import utils.ExecuteInit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
/**
 * 
 * @author Sacumen (www.sacumen.com)
 * 
 * It takes BioCatch end point URL and customer id from configuration parameter and execute BioCatch "init" API. 
 *
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class, configClass = BioCatchSessionNode.Config.class, tags = {"marketplace", "trustnetwork"})
public class BioCatchSessionNode extends AbstractDecisionNode {
	
	private ExecuteInit init;
	private final Config config;
	private static  Logger logger = LoggerFactory.getLogger(BioCatchSessionNode.class);
	private String loggerPrefix = "[BioCatch Session Node][Marketplace] ";
	
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
	    try {

            String userName = context.sharedState.get(SharedStateConstants.USERNAME).asString();
            String sessionId = context.sharedState.get(CUSTOMER_SESSION_ID).asString();

            logger.error(userName);
            logger.error(sessionId);


            // Creating Json Request
            JSONObject json = new JSONObject();
            json.put(CUSTOMER_ID,config.customerId());
            json.put(ACTION,INIT);
            json.put(BioCatchConsts.UUID,userName);
            json.put(CUSTOMER_SESSION_ID, sessionId);

            // Executing BioCatch "init" API
            int status = init.init(json,config.biocatchEndPoint());
            logger.error(Integer.toString(status));
            if(status == OK) {
                logger.error(loggerPrefix + "OK");
                return Action.goTo("True").build();
            }
            else {
                logger.error(loggerPrefix + "NOT OK");
                return Action.goTo("False").build();
            }
		} catch (Exception ex) {
          	logger.error(loggerPrefix + "Exception occurred: " + ex.getMessage());
          	logger.error(loggerPrefix + "Exception occurred: " + ex.getStackTrace());
          	ex.printStackTrace();
          	context.getStateFor(this).putShared(loggerPrefix + "Exception", new Date() + ": " + ex.getMessage());
          	return Action.goTo("Error").build();
        }

    }

     public static final class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        /**
         * Outcomes Ids for this node.
         */
        static final String SUCCESS_OUTCOME = "True";
        static final String FALSE_OUTCOME = "False";
        static final String ERROR_OUTCOME = "Error";

        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {

            List<Outcome> results = new ArrayList<>(
                    Arrays.asList(
                            new Outcome(SUCCESS_OUTCOME, SUCCESS_OUTCOME)
                    )
            );
            results.add(new Outcome(FALSE_OUTCOME, FALSE_OUTCOME));
            results.add(new Outcome(ERROR_OUTCOME, ERROR_OUTCOME));

            return Collections.unmodifiableList(results);
        }
    }

}
