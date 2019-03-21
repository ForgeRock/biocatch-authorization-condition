package utils;

public class BioCatchConsts {

    public static final String BIOCATCH_ADVICE_KEY = "BIOCATCH";

    public static final int DEFAULT_TIMEOUT = 5000;

    // JsonPath
    public static String SCORE = "score";
    public static String POLICY_STATE = "bcStatus";

    // request header
    static String APPLICATION_JSON = "application/json";
    static String NO_CACHE = "no-cache";
    
    // request body
    public static String CUSTOMER_ID = "customerID";
    public static String ACTION = "action";
    public static String UUID = "uuid";
    public static String CUSTOMER_SESSION_ID = "customerSessionID";
    
    //request action
    public static String INIT = "init";
    public static String GET_SCORE = "getScore";
    
    //Advice
    public static String CustomerSessionId_IS_NOT_PRESENT = "Please enter customerSessionID";
    public static String LEVEL_IS_NOT_SET_PROPERLY = "Please Enter either < or > or =";
    public static String NULL_RESPONSE = "Not able to get response from BioCatch getScore API";
    
    //response
    public static String GET_SCORE_SUCCESS_STATUS = "tested";
    
    //log(1=error, 2=warning, 3=message)
    public static int DEBUG_lEVEL = 3;
    
    // Http Status Code
    public static int OK = 200;
    
     
    
   

    


    

    

    

    


}
