class BootStrap {
	def grailsApplication
	
	def init = {servletContext ->
	
		//this code should be refactored into the plugin
		grails.plugins.jsonapivariants.AnnotationMarshaller.getAllApiNames(grailsApplication.domainClasses).each { namespace ->
			grails.converters.JSON.createNamedConfig(namespace) { JSON ->
				for (domainClass in grailsApplication.domainClasses) {
					JSON.registerObjectMarshaller(new grails.plugins.jsonapivariants.AnnotationMarshaller<grails.converters.JSON>(domainClass, namespace))
				}
			}
		}
		
	}
}