import grails.converters.JSON
import grails.plugin.spock.IntegrationSpec

// @TestFor(User)
class AnnotationMarshallerSpec extends IntegrationSpec {
	User roger

	def setup() {
		roger = new User(email: 'roger@roger.com', screenName: 'Roger', twitterUsername:'RogerRocks')
		roger.addToPets new ViciousPet(name:'spikey', numberOfLegs: 8, likesTickling: false, licenceNumber: 123)
		roger.addToPets(name:'rover', numberOfLegs: 4, likesTickling: true)
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
		!toJsonAndBack(roger).pets
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
}