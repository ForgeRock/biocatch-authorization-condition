package com.biocatch.auth.policy;
import static utils.BioCatchConsts.ACTION;
import static utils.BioCatchConsts.CUSTOMER_ID;
import static utils.BioCatchConsts.CUSTOMER_SESSION_ID;
import static utils.BioCatchConsts.INIT;
import static utils.BioCatchConsts.OK;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.util.*;
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
import java.net.*;
import java.io.*;
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
import static utils.BioCatchConsts.CUSTOMER_SESSION_ID;
import java.util.ResourceBundle;
import com.google.common.collect.ImmutableList;
/**
 * 
 * @author Sacumen (www.sacumen.com)
 * 
 * It takes BioCatch end point URL and customer id from configuration parameter and execute BioCatch "init" API. 
 *
 */
@Node.Metadata(outcomeProvider = BioCatchSessionProfilerNode.BioCatchSessionProfilerNodeOutcomeProvider.class, configClass = BioCatchSessionProfilerNode.Config.class, tags = {"marketplace", "trustnetwork"})
public class BioCatchSessionProfilerNode extends AbstractDecisionNode {
	
	private ExecuteInit init;
	private final Config config;
	private static  Logger logger = LoggerFactory.getLogger(BioCatchSessionProfilerNode.class);
	private String loggerPrefix = "[BioCatch Session Profiler Node][Marketplace] ";
	private static final String BUNDLE = BioCatchSessionProfilerNode.class.getName();
	
	public interface Config {
        // Getting BioCatch Javascript
        @Attribute(order = 100, requiredValue = true)
        default String biocatchJavascriptURL() {
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
	public BioCatchSessionProfilerNode(@Assisted Config config,ExecuteInit init) {
		this.config = config;
		this.init = init;
	}

	 private String getScriptAsString(String javascriptURL) throws Exception {

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet request = new HttpGet(javascriptURL);

        CloseableHttpResponse response = httpClient.execute(request);
        String content = EntityUtils.toString(response.getEntity());
        return content;
    }


	private static String createClientSideScriptExecutorFunction(String script, String customerSessionID) {
            return String.format(
                    "(function(output) {\n" +
                            "    %s\n" +
                            "console.log('%s'); \n" +
                            "window.cdApi.setCustomerSessionId('%s'); \n" +
                            "window.cdApi.changeContext('LOGIN_PAGE'); \n" +
                            "}) (document);\n",
                    script, customerSessionID, customerSessionID
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
                return Action.goTo(BioCatchSessionProfilerNodeOutcome.NEXT_OUTCOME.name()).build();
            }
	        String myScript = getScriptAsString(config.biocatchJavascriptURL());
	        RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
            String customerSessionID = generator.generate(30);
            context.sharedState.put(CUSTOMER_SESSION_ID, customerSessionID);
            String clientSideScriptExecutorFunction = createClientSideScriptExecutorFunction(myScript, customerSessionID);

            return send(Arrays.asList(new ScriptTextOutputCallback(clientSideScriptExecutorFunction), new HiddenValueCallback("Biocatch", "false"))).replaceSharedState(sharedState).build();
        } catch(Exception ex) {
            logger.error(loggerPrefix + "Exception occurred: " + ex.getMessage());
            ex.printStackTrace();
            context.getStateFor(this).putShared(loggerPrefix + "Exception", new Date() + ": " + ex.toString());
            return Action.goTo(BioCatchSessionProfilerNodeOutcome.ERROR_OUTCOME.name()).build();
        }

    }

    /**
     * The possible outcomes for the BioCatchSessionProfiler node.
     */
    public enum BioCatchSessionProfilerNodeOutcome {
        /**
         * Successful Found User.
         */
        NEXT_OUTCOME,
        /**
         * Error occured. Need to check sharedstate for issue
         */
        ERROR_OUTCOME
    }

    public static class BioCatchSessionProfilerNodeOutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE,
                   BioCatchSessionProfilerNodeOutcomeProvider.class.getClassLoader());
            return ImmutableList.of(
                    new Outcome(BioCatchSessionProfilerNodeOutcome.NEXT_OUTCOME.name(), bundle.getString("nextOutcome")),
                    new Outcome(BioCatchSessionProfilerNodeOutcome.ERROR_OUTCOME.name(), bundle.getString("errorOutcome")));
        }
    }



}