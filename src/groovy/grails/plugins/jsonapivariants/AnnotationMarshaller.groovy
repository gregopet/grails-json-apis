package grails.plugins.jsonapivariants
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty


/**
 * This class implements the JSON serialization of domain objects
 * according to the annotations declared on these objects. A new
 * instance is generated for each domain class - api namespace combo.
 * It also contains static helper methods to help with reflection.
 */
class AnnotationMarshaller<T> implements ObjectMarshaller<T> {
	protected final DefaultGrailsDomainClass forClass
	protected final List<GrailsDomainClassProperty> propertiesToSerialize
	
	/**
	 * Finds all API names defined in all domain classes.
	 * @return A set of all found namespace names.
	 */
	static Set getAllApiNames(domainClasses) {
		def namespaces = new HashSet<String>()
		domainClasses.each { domainClass ->
			domainClass.clazz.declaredFields.each { field ->
				def declaredNamespaces = field.getAnnotation(Api)?.value()
				declaredNamespaces?.each { apiNamespace -> namespaces << apiNamespace}
			}
		}
		return namespaces
	}
	
	/**
	 * Constructor: constructs a new marshaller for a given domain class - namespace
	 * combo.
	 * @param matchedDomainClass A grails domain class descriptor for which we are registering this marshaller.
	 * @param namespace Name of the namespace for which we are registering this marshaller.
	 */
	public AnnotationMarshaller(DefaultGrailsDomainClass matchedDomainClass, String namespace) {
		this.forClass = matchedDomainClass
		this.propertiesToSerialize = []
		def inspectedClass = matchedDomainClass.clazz
		while (inspectedClass) {
			inspectedClass.declaredFields
				.findAll{ it.getAnnotation(Api)?.value()?.size() == 0 || it.getAnnotation(Api)?.value()?.contains(namespace) }
				.each { propertiesToSerialize << forClass.getPropertyByName(it.name) }
			inspectedClass = inspectedClass.superclass
		}
	}
	
	/**
	 * Returns true if the given object can be serialized by this marshaller
	 * instance. Part of the ObjectMarshaller interface.
	 * @param object The object we are querying about.
	 */
	public boolean supports (Object object) {
		return object.class.isAssignableFrom(forClass.clazz)
	}
	
	/**
	 * Marshalls a given object according to the rules of the API namespace
	 * for which this marshaller was created. Part of the ObjectMarshaller inteface.
	 * @param object The object we are serializing.
	 * @param converter The converter instance that is performing the serialization.
	 */
	public void marshalObject(Object object, T converter) {
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