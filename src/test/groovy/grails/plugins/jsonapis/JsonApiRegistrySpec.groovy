import grails.converters.JSON
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.core.GrailsDomainClass
import org.grails.core.DefaultGrailsDomainClass
import spock.lang.*
import grails.plugins.jsonapis.*

import testapi.*
import org.grails.core.legacy.LegacyGrailsDomainClass

class JsonApiRegistrySpec extends Specification {
	GrailsDomainClass domainClass
	GrailsApplication grailsApplication
	JsonApiRegistry registry
	
	def setup() {
		domainClass = Mock(GrailsDomainClass)
		grailsApplication = Stub(GrailsApplication) {
			getArtefacts('Domain') >> [domainClass]
		}
		registry = new JsonApiRegistry()
	}

	def "during live reloads .updateMarshallers should mark registered but unannotated marshallers as deleted"() {
		given: 'a registry with a registered marshaller not present in domain class annotations'
		AnnotationMarshaller<JSON> marshaller = new AnnotationMarshaller<JSON>(new DefaultGrailsDomainClass(ViciousPet), "non-existant-api")
		def app = Stub(GrailsApplication)
		registry.marshallersByApi['non-existant-api'].add(marshaller)
		
		when: 'updating marshallers from annotations'
		registry.updateMarshallers(app)
		
		then: 'no-longer existing marshaller is marked as deleted'
		marshaller.deleted
	}

	def ".registerMarshaller allows registering individual marshallers in unit tests"() {
		given:
		JsonApiRegistry.registerMarshaller("detailedInformation", ViciousPet)

		when:
		String marshalledPet
		JSON.use("detailedInformation") {
			marshalledPet = new JSON(new ViciousPet(licenceNumber:1234, likesTickling: true)).toString()
		}

		then:
		marshalledPet.contains('licenceNumber')
		marshalledPet.contains('1234')
		!marshalledPet.contains('likesTickling')
	}
	
	@Ignore //it's hard to test this :(
	def "during live reloads .updateMarshallers should add any annotated marshallers to the existing ones"() {
		when: 'a class is scanned for declared APIs'
		def anotherDomainClass = new LegacyGrailsDomainClass(User)
		grailsApplication.domainClasses = [domainClass, anotherDomainClass]
		registry.updateMarshallers(grailsApplication)
		
		then: 'new class should have its marshallers added to the registry'
		registry.marshallersByApi.any { api, marshallers ->
			marshallers.any { it.forClass == anotherDomainClass }
		}
		
		and: 'marshallers of the old class should remain in the registry'
		registry.marshallersByApi.any { api, marshallers ->
			marshallers.any { it.forClass == domainClass }
		}
	}
	
	@Ignore //it's hard to test this :(
	def "during live reloads .updateMarshallers should rescan which properties to serialize"() {
		given: 'an API registry with a mocked marshaller'
		def marshaller = Mock(new AnnotationMarshaller<Pet>().class, constructorArgs:[domainClass, 'petDetails'])
		registry.marshallersByApi['petDetails'] << marshaller
		
		when:
		registry.updateMarshallers(grailsApplication)
		
		then:
		1 * marshaller.initPropertiesToSerialize()
	}
}