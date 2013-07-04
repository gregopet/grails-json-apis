import grails.converters.JSON
import grails.plugins.jsonapivariants.AnnotationMarshaller

class JsonApiVariantsGrailsPlugin {
    def version = "0.1"
    def grailsVersion = "2.0 > *"
    def pluginExcludes = [
        "grails-app/views/**",
        "grails-app/domain/**",
        "grails-app/controllers/**",
        "grails-app/i18n/**",
        "web-app/**",
        "demo-output.html"
    ]

    def title = "Grails Json Api Variants Plugin"
    def author = "Gregor Petrin"
    def authorEmail = "gregap@gmail.com"
    def description = '''\
Allows developers to declaratively define various JSON serialization
profiles and use them to marshall Grails domain classes at different
levels of detail or from different starting points in the object
graph.
'''

    def documentation = "https://github.com/gregopet/grails-json-api-variants"

    def license = "APACHE"
    def issueManagement = [ system: "github", url: "https://github.com/gregopet/grails-json-api-variants/issues" ]

    def scm = [ url: "https://github.com/gregopet/grails-json-api-variants" ]

    def doWithApplicationContext = { applicationContext ->
        //Generate and register the required ObjectMarshaller instances.
        AnnotationMarshaller.getAllApiNames(application.domainClasses).each { namespace ->
            JSON.createNamedConfig(namespace) { JSON ->
                for (domainClass in application.domainClasses) {
                    JSON.registerObjectMarshaller(new AnnotationMarshaller<JSON>(domainClass, namespace))
                }
            }
        }
    }
}
