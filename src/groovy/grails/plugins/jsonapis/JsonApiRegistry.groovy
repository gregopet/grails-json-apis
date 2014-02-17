package grails.plugins.jsonapis

import grails.converters.JSON
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * Keeps track of all the JSON APIs registered by the plugin. Constains methods
 * that are called on live reload events to update the APIs.
 */
class JsonApiRegistry {
	
	/**
	 * Updates the state of all registered marshallers, adding new ones or
	 * deleting existing (in case of a live reload).
	 */
	void updateMarshallers(GrailsApplication application) {
		getAllApiNames(application.domainClasses).each { namespace ->
			JSON.createNamedConfig(namespace) { JSON ->
				for (domainClass in application.domainClasses) {
					def marshaller = new AnnotationMarshaller<JSON>(domainClass, namespace)
					JSON.registerObjectMarshaller(marshaller)
					//marshallersByDomainClassAndApi[domainClass][namespace] = marshaller
				}
			}
		}
	}
	
	/**
	 * Finds all API names defined in all domain classes.
	 * @return A set of all found namespace names.
	 */
	Set getAllApiNames(domainClasses) {
		Set namespaces = []
		domainClasses.each { domainClass ->
			domainClass.properties.each { groovyProperty ->
				def declaredNamespaces = getPropertyAnnotationValue( domainClass.clazz, groovyProperty.name )?.value()
				declaredNamespaces?.each { apiNamespace -> namespaces << apiNamespace }
			}
		}
		return namespaces
	}
	
	/**
	 * Finds the method or field corresponding to a Groovy property name and its JsonApi annotation if it exists.
	 */
	static JsonApi getPropertyAnnotationValue(Class clazz, String propertyName) {
		while (clazz) {
			def fieldOrMethod = clazz.declaredFields.find { it.name == propertyName } ?: clazz.declaredMethods.find { it.name == 'get' + propertyName.capitalize() }
			if (fieldOrMethod) return fieldOrMethod?.getAnnotation(JsonApi)
			else clazz = clazz.superclass
		}
		return null
	}


}