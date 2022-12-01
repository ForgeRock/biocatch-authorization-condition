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
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import static org.forgerock.openam.auth.node.api.Action.send;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;
import org.apache.commons.text.RandomStringGenerator;
import javax.security.auth.callback.TextOutputCallback;
import org.forgerock.openam.auth.node.api.OutputState;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;
import com.google.common.base.Strings;
/**
 * 
 * @author Sacumen (www.sacumen.com)
 * 
 * It takes BioCatch end point URL and customer id from configuration parameter and execute BioCatch "init" API. 
 *
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = BioCatchSessionProfilerNode.Config.class, tags = {"marketplace", "trustnetwork"})
public class BioCatchSessionProfilerNode extends SingleOutcomeNode {
	
	private ExecuteInit init;
	private final Config config;
	private static  Logger logger = LoggerFactory.getLogger(BioCatchSessionProfilerNode.class);
	private String loggerPrefix = "[BioCatch Session Profiler Node][Marketplace] ";
	
	public interface Config {


	}
	
	/**
	 * 
	 * @param config 
	 * 
	 * Injecting dependency
	 */
	@Inject
	public BioCatchSessionProfilerNode(@Assisted Config config,ExecuteInit init) {
		this.config = config;
		this.init = init;
	}

	 private String getScriptAsString(String filename, String outputParameterId) {
        try {
            Reader paramReader = new InputStreamReader(getClass().getResourceAsStream(filename));

            StringBuilder data = new StringBuilder();
            BufferedReader objReader = new BufferedReader(paramReader);
            String strCurrentLine;
            while ((strCurrentLine = objReader.readLine()) != null) {
                data.append(strCurrentLine).append(System.lineSeparator());
            }
            return data.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


	private static String createClientSideScriptExecutorFunction(String script) {
	        RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
            String randomLetters = generator.generate(20);
            return String.format(
                    "(function(output) {\n" +
                            "    %s\n" + // script
                            "window.cdApi.setCustomerSessionId('%s'); \n" +
                            "window.cdApi.changeContext('LOGIN_PAGE'); \n" +
                            "window.cdApi.setCustomerBrand('BrandA'); \n" +
                            "}) (document);\n",
                    script, randomLetters
            );
        }
	

	/**
	 * It executes BioCatch "init" API.
	 */
	@Override
	public Action process(TreeContext context) {
	    try {
	        JsonValue sharedState = context.sharedState;
	        Optional<String> result = context.getCallback(HiddenValueCallback.class).map(HiddenValueCallback::getValue).
                            filter(scriptOutput -> !Strings.isNullOrEmpty(scriptOutput));
	         if (result.isPresent()) {
                return goToNext().replaceSharedState(sharedState).build();
            }
	        String myScript = getScriptAsString("biocatch-script.js", "myHiddenOutcome");
            String clientSideScriptExecutorFunction = createClientSideScriptExecutorFunction(myScript);

            return send(Arrays.asList(new ScriptTextOutputCallback(clientSideScriptExecutorFunction), new HiddenValueCallback("Biocatch", "false"))).replaceSharedState(sharedState).build();
        } catch(Exception ex) {
            logger.error(loggerPrefix + "Exception occurred: " + ex.getMessage());
            logger.error(loggerPrefix + "Exception occurred: " + ex.getStackTrace());
            ex.printStackTrace();
            context.getStateFor(this).putShared(loggerPrefix + "Exception", new Date() + ": " + ex.getMessage());
            return null;
        }



    }



}
