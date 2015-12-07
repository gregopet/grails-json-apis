package grails.plugins.jsonapis

import grails.core.GrailsDomainClass
import groovy.util.logging.Log

import grails.core.GrailsDomainClassProperty
import org.grails.web.converters.marshaller.ObjectMarshaller

/**
 * This class implements the JSON serialization of domain objects
 * according to the annotations declared on these objects. A new
 * instance is generated for each domain class - api namespace combo.
 * It also contains static helper methods to help with reflection.
 */
@Log
class AnnotationMarshaller<T> implements ObjectMarshaller<T> {
	final GrailsDomainClass forClass
	protected List<GrailsDomainClassProperty> propertiesToSerialize
	protected apiName
	
	/**
	 * Set to true if an API was removed via a live reload event so user
	 * can be notified when trying to marshall a class using this deleted
	 * API.
	 */
	boolean deleted = false
	void setDeleted(boolean value) {
		if (value != this.deleted) {
			if (value) log.info "Domain class ${forClass.clazz.name} will no longer serialize under namespace $apiName"
			else log.info "Domain class ${forClass.clazz.name} will again serialize under namespace $apiName"
			this.deleted = value
		}
	}
	
	/**
	 * Sets or updates the names of those properties that will be serialized by given API.
	 */
	void initPropertiesToSerialize() {
		this.propertiesToSerialize = forClass.properties.findAll { groovyProperty ->
			def annotation = JsonApiRegistry.getPropertyAnnotationValue( forClass.clazz, groovyProperty.name )
			return annotation && (!annotation.value() || apiName in annotation.value())
		}
	}

	/**
	 * Constructor: constructs a new marshaller for a given domain class - namespace combo.
	 * @param matchedDomainClass A grails domain class descriptor for which we are registering this marshaller.
	 * @param namespace Name of the namespace for which we are registering this marshaller.
	 */
	AnnotationMarshaller(GrailsDomainClass matchedDomainClass, String namespace) {
		this.forClass = matchedDomainClass
		this.deleted = false
		this.apiName = namespace
		initPropertiesToSerialize()
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
		if (deleted) throw new org.grails.web.converters.exceptions.ConverterException("Converter Configuration with name '${apiName}' was removed!")
		converter.build {
			"${forClass.identifier.name}"(object."${forClass.identifier.name}") //always put the ID property into the object..
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
