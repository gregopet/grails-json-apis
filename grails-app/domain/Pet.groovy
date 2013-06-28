import grails.plugins.jsonapivariants.Api

class Pet {
	static belongsTo = [user:User]

	@Api
	String name
	
	@Api('detailedInformation')
	Integer numberOfLegs
}