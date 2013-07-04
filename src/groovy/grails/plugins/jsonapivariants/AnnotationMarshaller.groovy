package grails.plugins.jsonapivariants

import groovy.util.logging.Log

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller

/**
 * This class implements the JSON serialization of domain objects
 * according to the annotations declared on these objects. A new
 * instance is generated for each domain class - api namespace combo.
 * It also contains static helper methods to help with reflection.
 */
@Log
class AnnotationMarshaller<T> implements ObjectMarshaller<T> {
	protected final DefaultGrailsDomainClass forClass
	protected final List<GrailsDomainClassProperty> propertiesToSerialize

	/**
	 * Finds all API names defined in all domain classes.
	 * @return A set of all found namespace names.
	 */
	static Set getAllApiNames(domainClasses) {
		Set namespaces = []
		domainClasses.each { domainClass ->
			domainClass.properties.each { groovyProperty ->
				def declaredNamespaces = getPropertyAnnotationValue( domainClass.clazz, groovyProperty.name )
				declaredNamespaces?.each { apiNamespace -> namespaces << apiNamespace }
			}
		}
		log.info "Found following JSON namespaces: ${namespaces.join(', ')}"
		return namespaces
	}

	/**
	 * Finds the method or field corresponding to a Groovy property name.
	 */
	private static getPropertyAnnotationValue(Class clazz, String propertyName) {
		while (clazz) {
			def fieldOrMethod = clazz.declaredFields.find { it.name == propertyName } ?: clazz.declaredMethods.find { it.name == 'get' + propertyName.capitalize() }
			if (fieldOrMethod) return fieldOrMethod?.getAnnotation(Api)?.value()
			else clazz = clazz.superclass
		}
		return null
	}

	/**
	 * Constructor: constructs a new marshaller for a given domain class - namespace combo.
	 * @param matchedDomainClass A grails domain class descriptor for which we are registering this marshaller.
	 * @param namespace Name of the namespace for which we are registering this marshaller.
	 */
	AnnotationMarshaller(DefaultGrailsDomainClass matchedDomainClass, String namespace) {
		this.forClass = matchedDomainClass
		this.propertiesToSerialize = matchedDomainClass.properties.findAll { groovyProperty ->
			def annotationValue = AnnotationMarshaller.getPropertyAnnotationValue( matchedDomainClass.clazz, groovyProperty.name )
			return !annotationValue || annotationValue.contains(namespace)
		}
	}

	/**
	 * Returns true if the given object can be serialized by this marshaller
	 * instance. Part of the ObjectMarshaller interface.
	 * @param object The object we are querying about.
	 */
	boolean supports(object) {
		return object.class.isAssignableFrom(forClass.clazz)
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
