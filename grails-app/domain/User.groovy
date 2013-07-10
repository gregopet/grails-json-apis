import grails.plugins.jsonapis.JsonApi

class User {

	static hasMany = [
		pets: Pet
	]

	//Annotating a property with @Api and no additional parameters
	//means this property should appear in all API versions
	@JsonApi
	String screenName
	
	
	@JsonApi('userSettings')
	String email
	String getEmail() {
		"${screenName}<${email}>"
	}
	
	@JsonApi(['userSettings', 'detailedInformation', 'social'])
	String twitterUsername
	
	@JsonApi(['detailedInformation', 'userSettings'])
	Set pets
	
	String neverGetsSerialized
	
	@JsonApi('detailedInformation')
	Integer getNumberOfTicklyAnimals() {
		pets.count { it.likesTickling }
	}
}