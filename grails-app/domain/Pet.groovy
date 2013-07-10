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
}