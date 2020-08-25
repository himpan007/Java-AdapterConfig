# Adapter CLI - SSH and Telnet implementations.

The CLI archetype is the most commonly used archetype. It supports both Telnet and SSH transports.

The archetype has stub request handler classes that are preassigned to product requests in the "requestCapabilities.xml" file.
A complete set of request handlers for all products is included. It is up to the adapter developer to decided which classes to implement. 

The goal should always be to implement all of the request classes, but there may be cases where only a subset are required. 

It is the responsibility of the adapter developer to modify the "capabilityProfiles.xml" and the "requestProfiles.xml" 
if the adapter contains only a partial implementation of the request handlers.

## supported Requests in this archetype

* crud
	* NESecurityCreateUserRequestHander.java
	* NESecurityDeleteUserRequestHandler.java
	* NESecurityEditMyPasswordRequestHandler.java
	* NESecurityEditUserRequestHandler.java
	
* discovery
	* NeighboutRequestHandler.java
	
* gs
  * data
  	* collecting
  		* CSVReader.java
  		* csvstruct
  			* AttributeRulesCSVStruct.java
			* ReadRulesCSVStruct.java
			* ResourceRulesCSVStruct.java
			
  * reconcile
  	* ReconcileCommon.java
  	* attribute
  		* CreateResourcesRawAttributesRequestHandler.java
  		* DeleteResourcesRawAttributesRequestHandler.java
  		* EditResourcesRawAttributesRequestHandler.java
  		* ReconcileAttributeBuilder.java
  		* ReconcileCommandData.java
  	* command
  		* CommandResourceRawAttributeRequestHandler.java
  		* CommandResourceRequestHandler.java
  	* resource
  		* CreateResourceRequestHandler.java
  		* DeleteResourceRequestHandler.java
  		* ResourceRuleCommands.java
  		
  * resource
  	* retrieval
  		* RetrieveResourceRawAttributesRequestHandler.java
  		* RetrieveUserRawAttributes.java
  		
* pe
  * NESecurityEscalatePriviledgeRequestHandler.java
  
* sso
  * FingerprintRequestHandler.java
  * KeepaliveRequestHandler.java
  * LoginRequestHandler.java
  * LogoutRequestHandler.java
  