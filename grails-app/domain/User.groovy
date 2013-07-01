import grails.plugins.jsonapivariants.Api

class User {

	static hasMany = [
		pets: Pet
	]

	//Annotating a property with @Api and no additional parameters
	//means this property should appear in all API versions
	@Api
	String screenName
	
	
	@Api('userSettings')
	String email
	String getEmail() {
		"${screenName}<${email}>"
	}
	
	@Api(['userSettings', 'detailedInformation', 'social'])
	String twitterUsername
	
	@Api(['detailedInformation', 'userSettings'])
	Set pets
	
	@Api('detailedInformation')
	Integer getNumberOfTicklyAnimals() {
		Pets.countAllWhereIsLikesTickling()
	}
}