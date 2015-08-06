import grails.converters.JSON
import grails.web.JSONBuilder
import testapi.*

import spock.lang.*
import grails.test.mixin.integration.Integration

@Integration
class AnnotationMarshallerSpec extends Specification {
	User roger

	def setup() {
		roger = new User(email: 'roger@roger.com', screenName: 'Roger', twitterUsername:'RogerRocks', neverGetsSerialized:'some_value')
		def pet1 = new ViciousPet(name:'spikey', numberOfLegs: 8, likesTickling: false, licenceNumber: 123, user: roger)
		def pet2 = new Pet(name:'rover', numberOfLegs: 4, likesTickling: true, user: roger)
		roger.pets = [pet1, pet2]
	}

	/**
	 * Helper method: serializes an object to JSON and then changes it
	 * right back into a Groovy object
	 */
	def toJsonAndBack(object) {
		JSON.parse((object as JSON).toString())
	}

	def "Marshaller should serialize properties explicitly belonging to the requested API"() {
		when:
		JSON.use("userSettings")

		then:
		toJsonAndBack(roger).twitterUsername == roger.twitterUsername
		toJsonAndBack(roger).pets.first().likesTickling != null
	}

	def "Marshaller should serialize properties belonging to all APIs"() {
		when:
		JSON.use("userSettings")

		then:
		toJsonAndBack(roger).screenName == roger.screenName
		toJsonAndBack(roger).pets.first().name
	}

	def "Marshaller should not serialize properties that aren't part of the requested API"() {
		when:
		JSON.use("detailedInformation")

		then:
		!toJsonAndBack(roger).email
		toJsonAndBack(roger).pets.first().likesTickling == null
	}

	def "Marshaller should process collections that belong to an API"() {
		when:
		JSON.use("detailedInformation")

		then:
		toJsonAndBack(roger).pets.size()
	}

	def "Marshaller should not process collections that do not belong to an API"() {
		when:
		JSON.use("social")

		then:
		!toJsonAndBack(roger).neverGetsSerialized
	}

	def "Marshaller should process 'belongsTo' parent relations that belong to an API"() {
		when:
		JSON.use("petDetails")

		then:
		toJsonAndBack(roger.pets.first()).user.screenName == 'Roger'
	}

	def "Marshaller should not process 'belongsTo' parent relations that don't belong to an API"() {
		when:
		JSON.use("detailedInformation")

		then:
		!toJsonAndBack(roger.pets.first()).user
	}

	def "Marshaller should work on custom getters"() {
		when:
		JSON.use("userSettings")

		then:
		toJsonAndBack(roger).email == roger.getEmail()
	}

	def "Marshaller should work for getters not backed by a Grails field"() {
		when:
		JSON.use("detailedInformation")

		then:
		toJsonAndBack(roger).numberOfTicklyAnimals == roger.pets.count { it.likesTickling }
	}
	
	def "Marshaller should work on properties defined in a superclass"() {
		when:
		JSON.use("detailedInformation")
		
		then:
		toJsonAndBack(roger).pets.find { it.licenceNumber }.name
	}
	
	def "Marshaller should work on objects rendered by a JSONBuilder"() {
		when:
		JSON.use("userSettings")
		def serializedObj = new JSONBuilder().build {
			owner = roger
			pet = roger.pets.first()
		}
		def response = JSON.parse(serializedObj.toString());
		
		then:
		response.owner.twitterUsername
		!response.owner.numberOfTicklyAnimals
		response.pet.likesTickling != null
		!response.pet.numberOfLegs
		
	
	}
}