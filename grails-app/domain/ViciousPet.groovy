import grails.plugins.jsonapivariants.Api

class ViciousPet extends Pet {
	
	@Api('detailedInformation')
	Integer licenceNumber

}