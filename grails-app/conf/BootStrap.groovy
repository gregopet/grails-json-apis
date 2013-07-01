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
		
		//some demo data to populate our view..
		def person = new User(screenName:'Nelson', email:'nelson@example.org', twitterUsername: 'nelsonIsGod')
		person.addToPets(name: 'Rover', numberOfLegs: 4, likesTickling: true)
		person.addToPets(name: 'Spidey', numberOfLegs: 8, likesTickling: false)
		person.addToPets(name: 'Venom', numberOfLegs: 0, likesTickling: false)
		person.save(failOnError:true)
	}
}