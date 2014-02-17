package grails.plugins.jsonapis

import groovy.util.logging.Log

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.web.converters.Converter
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller

/**
 * This class implements the JSON serialization of domain objects
 * according to the annotations declared on these objects. A new
 * instance is generated for each domain class - api namespace combo.
 * It also contains static helper methods to help with reflection.
 */
@Log
class AnnotationMarshaller<T extends Converter> implements ObjectMarshaller<T> {
	protected final DefaultGrailsDomainClass forClass
	protected List<GrailsDomainClassProperty> propertiesToSerialize
	
	/**
	 * Sets or updates the names of those properties that will be serialized by given API.
	 */
	void initPropertiesToSerialize(DefaultGrailsDomainClass matchedDomainClass, String namespace) {
		this.propertiesToSerialize = matchedDomainClass.properties.findAll { groovyProperty ->
			def annotation = JsonApiRegistry.getPropertyAnnotationValue( matchedDomainClass.clazz, groovyProperty.name )
			return annotation && (!annotation.value() || namespace in annotation.value())
		}
	}

	/**
	 * Constructor: constructs a new marshaller for a given domain class - namespace combo.
	 * @param matchedDomainClass A grails domain class descriptor for which we are registering this marshaller.
	 * @param namespace Name of the namespace for which we are registering this marshaller.
	 */
	AnnotationMarshaller(DefaultGrailsDomainClass matchedDomainClass, String namespace) {
		this.forClass = matchedDomainClass
		initPropertiesToSerialize(matchedDomainClass, namespace)
		log.info "Domain class ${matchedDomainClass.clazz.name} will serialize following properties under namespace $namespace: ${propertiesToSerialize.collect { it.name }.join(', ')}"
	}

	/**
	 * Returns true if the given object can be serialized by this marshaller
	 * instance. Part of the ObjectMarshaller interface.
	 * @param object The object we are querying about.
	 */
	boolean supports(object) {
		return forClass.clazz == object.getClass()
	}

	/**
	 * Marshalls a given object according to the rules of the API namespace
	 * for which this marshaller was created. Part of the ObjectMarshaller inteface.
	 * @param object The object we are serializing.
	 * @param converter The converter instance that is performing the serialization.
	 */
	void marshalObject(object, T converter) {
		converter.build {
			"${forClass.identifier.name}"(object.ident()) //always put the ID property into the object..
			for (prop in propertiesToSerialize) {
				if (prop.oneToMany) {
					"$prop.name" {
						for (child in object."${prop.name}") {
							converter.convertAnother child
						}
					}
				} else {
					"$prop.name"(object."$prop.name")
				}
			}
		}
	}
}
