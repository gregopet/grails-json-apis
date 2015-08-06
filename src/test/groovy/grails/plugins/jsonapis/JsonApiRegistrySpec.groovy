import spock.lang.*
import grails.plugins.jsonapis.*

import testapi.*
import org.grails.core.legacy.LegacyGrailsDomainClass
import org.grails.core.legacy.LegacyGrailsApplication
import grails.test.mixin.integration.Integration

class JsonApiRegistrySpec extends Specification {
	LegacyGrailsDomainClass domainClass
	LegacyGrailsApplication grailsApplication
	JsonApiRegistry registry
	
	def setup() {
		domainClass = new LegacyGrailsDomainClass()
		grailsApplication = new LegacyGrailsApplication()
		grailsApplication.metaClass.getDomainClasses = { [domainClass] }
		registry = new JsonApiRegistry()
	}

	def "during live reloads .updateMarshallers should mark registered but unannotated marshallers as deleted"() {
		given: 'a registry with a registered marshaller not present in domain class annotations'
		def marshaller = Mock(AnnotationMarshaller)
		def app = Stub(LegacyGrailsApplication) {
			getProperty('domainClasses') >> []
		}
		registry.marshallersByApi['non-existant-api'] << marshaller
		
		when: 'updating marshallers from annotations'
		registry.updateMarshallers(app)
		
		then: 'no-longer existing marshaller is marked as deleted'
		1 * marshaller.setProperty('deleted', true)
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