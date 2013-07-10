import grails.plugins.jsonapis.JsonApi

class ViciousPet extends Pet {
	
	@JsonApi('detailedInformation')
	Integer licenceNumber

}