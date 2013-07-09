import grails.plugins.jsonapivariants.JsonApi

class ViciousPet extends Pet {
	
	@JsonApi('detailedInformation')
	Integer licenceNumber

}