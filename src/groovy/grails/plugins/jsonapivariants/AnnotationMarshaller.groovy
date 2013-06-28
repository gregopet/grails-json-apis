package grails.plugins.jsonapivariants
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty


//test for users first!
class AnnotationMarshaller<T> implements ObjectMarshaller<T> {
	protected final DefaultGrailsDomainClass forClass
	protected final List<GrailsDomainClassProperty> propertiesToSerialize
	
	//Returns set of all namespace names
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
	
	public boolean supports (Object object) {
		return object.class.isAssignableFrom(forClass.clazz)
	}
	
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