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
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Sacumen (www.sacumen.com)
 * 
 * This class collects cuctomerSessionID from user and shared in shared state.
 *
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass =
BioCatchSessionCollectorNode.Config.class)
public class BioCatchSessionCollectorNode extends SingleOutcomeNode{

	private static final String BUNDLE = "com/biocatch/auth/policy/BioCatchSessionCollectorNode";

	
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
		

		return context.getCallback(NameCallback.class).map(NameCallback::getName)
					  .map(String::new).filter(name -> !Strings.isNullOrEmpty(name))
					  .map(name -> goToNext().replaceSharedState(sharedState.put(CUSTOMER_SESSION_ID, name))
											 .build()).orElseGet(() -> collectOTP(context));
	}

}
