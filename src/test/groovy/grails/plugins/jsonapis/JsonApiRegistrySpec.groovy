package grails.plugins.jsonapis

import grails.converters.JSON
import grails.core.GrailsApplication
import org.grails.datastore.mapping.keyvalue.mapping.config.KeyValueMappingContext
import org.grails.datastore.mapping.keyvalue.mapping.config.KeyValuePersistentEntity
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import spock.lang.*
import grails.plugins.jsonapis.*

import testapi.*

class JsonApiRegistrySpec extends Specification {
	PersistentEntity domainClass
	GrailsApplication grailsApplication
	JsonApiRegistry registry
	MappingContext mappingContext
	
	def setup() {
		mappingContext = new KeyValueMappingContext('pets')
		domainClass = mappingContext.addPersistentEntity(Pet)
		grailsApplication = Stub(GrailsApplication) {
		}
		grailsApplication.setMappingContext(mappingContext)
		registry = new JsonApiRegistry()
	}

	def "during live reloads .updateMarshallers should mark registered but unannotated marshallers as deleted"() {
		given: 'a registry with a registered marshaller not present in domain class annotations'
		def user = mappingContext.addPersistentEntity(User)
		AnnotationMarshaller<JSON> marshaller = new AnnotationMarshaller<JSON>(user, "non-existant-api")
		def app = Stub(GrailsApplication)
		registry.marshallersByApi['non-existant-api'].add(marshaller)
		
		when: 'updating marshallers from annotations'
		registry.updateMarshallers(app)
		
		then: 'a no-longer existing marshaller is marked as deleted'
		marshaller.deleted
	}

	def ".registerMarshaller allows registering individual marshallers in unit tests"() {
		given:
		PersistentEntity entity = mappingContext.addPersistentEntity(ViciousPet)
		JsonApiRegistry.registerMarshaller("detailedInformation", entity as PersistentEntity[])

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
		def anotherDomainClass = new KeyValuePersistentEntity(User, mappingContext)
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