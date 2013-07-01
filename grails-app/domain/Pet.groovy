import grails.plugins.jsonapivariants.Api

class Pet {
	static belongsTo = [
		user:User
	]
	
	@Api('petDetails') 
	User user

	@Api
	String name
	
	@Api('detailedInformation')
	Integer numberOfLegs
	
	@Api('userSettings')
	Boolean likesTickling
}