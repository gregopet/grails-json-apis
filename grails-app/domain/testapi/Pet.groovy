package testapi

import grails.plugins.jsonapis.JsonApi

class Pet {
	static belongsTo = [
		user:User
	]
	
	@JsonApi('petDetails') 
	User user

	@JsonApi
	String name
	
	@JsonApi('detailedInformation')
	Integer numberOfLegs
	
	@JsonApi('userSettings')
	Boolean likesTickling
	
	//This property serves to help unit tests detect
	//any bugs with setters-only properties.
	void setSomething(String password) {
	
	}
}