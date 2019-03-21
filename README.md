# BioCatch-Auth
A collection of nodes and authorization policies for for ForgeRock's [Identity Platform][forgerock_platform] 6.5 and 
above.

## BioCatch Information

BioCatch prevents fraud while providing online and mobile users with a frictionless experience. The platform develops behavioral biometric profiles of online users to recognize a wide range of human and non-human cybersecurity threats including malware, remote access trojans (RATs), and robotic activity (bots).

BioCatch provides: 
* Identity proofing – analyzing multiple dimensions of how information is entered such as 
application fluency, navigational fluency, and data familiarity to detect the use of stolen or synthetic identities 
in filling out online applications. 
* Continuous authentication – BioCatch selects 20 unique features from its 500+ 
patented behavioral profiling metrics to analyze a user's behavior throughout a session – without any disruption in 
the digital experience. It compares user behavior in real time against the profile to return an actionable risk score
. 
* Fraud Prevention – BioCatch goes beyond traditional solutions, circumventing the need to maintain malware or bot 
libraries or rely on known device IDs or IP addresses to catch fraud. The Invisible Challenges approach can identify MitB, device spoofing, and RAT-in-the-Browser, providing real-time fraud alerts and minimizing false alarms.

When BioCatch is integrated via ForgeRock, ForgeRock will handle all backend process, calling BioCatch API via the 
ForgeRock AuthZ engine and taking actions based on the BioCatch Score.

## Installation

The BioCatch-Auth tree nodes and BioCatch Condition Type will be packaged as a jar file using the maven build tool and will be deployed in to the ForgeRock Access Management (AM) application WEB-INF/lib folder which is running on tomcat server.

Copy the .jar file from the ../target directory into the ../web-container/webapps/openam/WEB-INF/lib directory where AM is deployed.

Edit translation.json file - under ../web-container/webapps/openam/XUI/locales/en

Search for "conditionTypes" and add to it the following:

```js
"BioCatchCondition": {
                 "title": "BioCatch Authorization Condition",
                 "props": {
                 "score": "Score",
                 "BioCatchEndPointUrl" : "BioCatch End Point URL",
	         "customerId" : "Customer ID",
		 "advice" : "Advice",
		 "level" : "Greater than or Less than or Equal to(< or > or =)",
		 "cacheExpirationTime" : "Cache Expiration Time (In Seconds)"
                            }
                        },
```

The file should look like this after editing:

```js
 "conditionTypes": {
         "BioCatchCondition": {
                 "title": "BioCatch Authorization Condition",
                 "props": {
                      "score": "Score",
		      "BioCatchEndPointUrl" : "BioCatch End Point URL",
		      "customerId" : "Customer ID",
		      "advice" : "Advice",
		      "level" : "Greater than or Less than or Equal to(< or > or =)",
		      "cacheExpirationTime" : "Cache Expiration Time (In Seconds)"
                            }
                        },
          "AMIdentityMembership": {
              "title": "Identity Membership",
              "props": {
                  "amIdentityName": "AM Identity Name"
              }
          },
          "AuthLevel": {
              "title": "Authentication Level (greater than or equal to)",
              "props": {
                  "authLevel": "Authentication Level"
              }
          },
          "AuthScheme": {
              "title": "Authentication by Module Instance",
              "props": {
                  "authScheme": "Authentication Scheme",
                  "applicationIdleTimeout": "Application Idle Timeout Scheme",
                  "applicationName": "Application Name"
              }
          },
          "AuthenticateToRealm": {
              "title": "Authentication to a Realm",
              "props": {
                  "authenticateToRealm": "Authenticate to a Realm"
              }
          }.....................
```

Restart the web container, the new condition will appear under Authorization -> Policy Set -> Policy -> Environments -> 
BioCatch Authorization Policy and the authentication nodes will be visible under Authnetication -> Tree > BioCatch Session Collector Node and BioCatch Session Node.

In case BioCatchCondition should be added to existing policy, follow the following explanation on how to update existing policy with new condition: https://backstage.forgerock.com/docs/am/6/authorization-guide/#add-custom-policy-impl-to-existing-apps

# BioCatch-Auth Tree Configuration

Following are the nodes that will be available after deploying the jar file:

* BioCatch Session Collector Node - This node will collect customerSessionID (the unique session identifier) from the
 end user to call getScore API. There are no configurable attributes for it.

* BioCatch Session Node - This node will call BioCatch init API to initialize a session, connecting the session with 
specific user, connecting the client and server.

Attributes to be configured are:
* BioCatch End Point : Endpoint URL of BioCatch to hit init API.
* Customer Id : The customer or project identifier provided by BioCatch.


Nodes appear like this : 
![bio_4](https://user-images.githubusercontent.com/20396535/53595251-c6696180-3bc2-11e9-8c41-6457ef0bedc5.PNG)


## Configure the trees as follows

 * Navigate to **Realm** > **Authentication** > **Trees** > **Create Tree**
 
 ![tree](https://user-images.githubusercontent.com/20396535/48189113-66c21e80-e365-11e8-8045-326786a41aca.PNG)


## Configuring BioCatch-Auth Tree

This section depicts configuration of BioCatch-Auth Auth Tree


* Configure BioCatch-Auth Tree as shown below

Tree Configuration : 
![bio_5](https://user-images.githubusercontent.com/20396535/53595348-06c8df80-3bc3-11e9-9a7a-1cea643e7be0.PNG)


# BioCatch-Auth Authorization Policy Configuration:
1. Log into ForgeRock AM console
2. Go to Authorization -> Policy Set -> {policy} -> Environments -> Add environment condition -> select BioCatch Authorization Condition

## Policy_1

![policy_1](https://user-images.githubusercontent.com/20396535/53596014-c4a09d80-3bc4-11e9-9bb9-b19c64e3a7f3.PNG)

![policy_01](https://user-images.githubusercontent.com/20396535/53596035-d7b36d80-3bc4-11e9-8c44-f551e7bc760b.PNG)


## Policy_2

![policy_2](https://user-images.githubusercontent.com/20396535/53596070-ed289780-3bc4-11e9-9f94-7264a030b873.PNG)

![policy_02](https://user-images.githubusercontent.com/20396535/53596086-fa458680-3bc4-11e9-9826-679ec153cba9.PNG)


## Policy_3

![policy_3](https://user-images.githubusercontent.com/20396535/53596126-134e3780-3bc5-11e9-9ccd-11f1cd7dc2c8.PNG)

![policy_03](https://user-images.githubusercontent.com/20396535/53596165-26f99e00-3bc5-11e9-9b22-40b642515b7f.PNG)


