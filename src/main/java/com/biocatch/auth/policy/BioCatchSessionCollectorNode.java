package com.biocatch.auth.policy;

import static org.forgerock.openam.auth.node.api.Action.send;
import static utils.BioCatchConsts.*;


import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.TextOutputCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.forgerock.util.i18n.PreferredLocales;

/**
 * 
 * @author Sacumen (www.sacumen.com)
 * 
 * This class collects cuctomerSessionID from user and shared in shared state.
 *
 */
@Node.Metadata(outcomeProvider = BioCatchSessionCollectorNode.BioCatchSessionCollectorNodeOutcomeProvider.class, configClass =
BioCatchSessionCollectorNode.Config.class, tags = {"marketplace", "trustnetwork"})
public class BioCatchSessionCollectorNode extends AbstractDecisionNode {

	private static final String BUNDLE = BioCatchSessionCollectorNode.class.getName();
	private String loggerPrefix = "[BioCatch Session Collector Node][Marketplace] ";

	
	public interface Config {
	}
	
	@Inject
	public BioCatchSessionCollectorNode() {
	}
	
	/**
	 * 
	 * @param context TreeContext
	 * @return Action
	 * 
	 * It executes callback to get session if from the user.
	 */
	private Action collectOTP(TreeContext context) {
		List<Callback> cbList = new ArrayList<>(2);
		ResourceBundle bundle = context.request.locales.getBundleInPreferredLocale(BUNDLE, getClass().getClassLoader());
		NameCallback ncb = new NameCallback(bundle.getString("callback.sessionId"), "Enter Session ID");
		TextOutputCallback tcb = new TextOutputCallback(0,"Please Enter Session Id");
		cbList.add(tcb);
		cbList.add(ncb);
		return send(ImmutableList.copyOf(cbList)).build();
	}
	
	
	/**
	 * @param context TreeContext
	 * @return Action
	 * Collects session id from user and send it to the next node.
	 */
	@Override
	public Action process(TreeContext context) {
		JsonValue sharedState = context.sharedState;
		if (!Strings.isNullOrEmpty(context.sharedState.get(CUSTOMER_SESSION_ID).asString()))  {
			return Action.goTo(BioCatchSessionCollectorNodeOutcome.NEXT_OUTCOME.name()).build();
		}

		return context.getCallback(NameCallback.class).map(NameCallback::getName)
				.map(String::new).filter(name -> !Strings.isNullOrEmpty(name))
				.map(name -> Action.goTo(BioCatchSessionCollectorNodeOutcome.NEXT_OUTCOME.name()).replaceSharedState(sharedState.put(CUSTOMER_SESSION_ID, name))
						.build()).orElseGet(() -> collectOTP(context));
	}

	public enum BioCatchSessionCollectorNodeOutcome {
		/**
		 * Successful Found User.
		 */
		NEXT_OUTCOME,
		/**
		 * Error occured. Need to check sharedstate for issue
		 */
		ERROR_OUTCOME
	}

	public static class BioCatchSessionCollectorNodeOutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
		@Override
		public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
			ResourceBundle bundle = locales.getBundleInPreferredLocale(BUNDLE,
					BioCatchSessionCollectorNodeOutcomeProvider.class.getClassLoader());
			return ImmutableList.of(
					new Outcome(BioCatchSessionCollectorNodeOutcome.NEXT_OUTCOME.name(), bundle.getString("nextOutcome")),
					new Outcome(BioCatchSessionCollectorNodeOutcome.ERROR_OUTCOME.name(), bundle.getString("errorOutcome")));
		}
	}

}
