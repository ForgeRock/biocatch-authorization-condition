try {
//=========================== Please update these variables ===========================
  var biocatchEndpoint = "";
  var customerId = "";
  var minScore = 0;
  var maxScore = 500;
  var advices = ["Fraud Alert"];
//=====================================================================================

  var customerSessionID = null;

  /**
   * Sends a request to Biocatch to get the score of a customer session.
   *
   * @returns {*} The score of a customer session.
   */
  function getScore() {

    var loginDoRequest = new org.forgerock.http.protocol.Request();

    //Set the method type.
    loginDoRequest.setMethod("POST");

    //set the POST URL
    loginDoRequest.setUri(biocatchEndpoint);

    //set some header values
    loginDoRequest.getHeaders().add('Content-Type', 'application/json; charset=UTF-8');

    var user = String(session.getProperty("UserToken"));

    //set some body values
    var theBody = JSON.stringify({
      "action": "getScore",
      "customerSessionID": customerSessionID,
      "uuid": user,
      "solution": "ATO",
      "activityType": "LOGIN",
      "customerID": customerId
    });

    loginDoRequest.getEntity().setString(theBody);
    var response = httpClient.send(loginDoRequest).get();

    var resultJSON = JSON.parse(response.getEntity().getString());
    return parseInt(resultJSON.score);
  }

  /**
   * Retrieve and validate the variables required to make the external HTTP calls.
   *
   * @returns {boolean} Will be true if validation was successful.
   */
  function validateAndInitializeParameters() {

    if (username == null || biocatchEndpoint == null || maxScore == null || customerId == null || advice == null || minScore == null)
      return false;

    if (!environment) {
      logger.warning("No environment parameters specified in the evaluation request.");
      return false;
    }

    if (environment.get("customerSessionID") != null && environment.get("customerSessionID").iterator().hasNext()) {
      customerSessionID = environment.get("customerSessionID").iterator().next();
    } else {
      logger.warning("No customerSessionId specified in the evaluation request environment parameters.");
      return false;
    }

    return true;
  }


  if (validateAndInitializeParameters()) {

    var scoreFromBiocatch = getScore();

    if (scoreFromBiocatch >= minScore && scoreFromBiocatch <= maxScore) {
      logger.message("Authorization Succeeded");
      authorized = true;
    } else {
      logger.message("Authorization Failed");
      advice.put("advice", advices);
      authorized = false;
    }

  } else {
    logger.message("Required parameters not found. Authorization Failed.");
    advice.put("advice", ["Required parameters not found"]);
    authorized = false;
  }
} catch (error) {
  logger.error(error);
  advice.put("advice", ["Error occured"]);
  authorized = false;
}