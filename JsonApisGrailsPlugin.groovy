import grails.plugins.jsonapis.JsonApiRegistry

class JsonApisGrailsPlugin {
    def version = "0.95"
    def grailsVersion = "2.0 > *"
    def pluginExcludes = [
        "grails-app/views/**",
        "grails-app/domain/**",
        "grails-app/controllers/**",
        "grails-app/i18n/**",
        "web-app/**",
        "demo-output.html"
    ]

    def title = "Grails Json Apis Plugin"
    def author = "Gregor Petrin"
    def authorEmail = "gregap@gmail.com"
    def description = '''\
Allows developers to declaratively define various JSON serialization
profiles and use them to marshall Grails domain classes at different
levels of detail or from different starting points in the object
graph.
'''

    def documentation = "https://github.com/gregopet/grails-json-apis"

    def license = "APACHE"
    def issueManagement = [ system: "github", url: "https://github.com/gregopet/grails-json-apis/issues" ]

    def scm = [ url: "https://github.com/gregopet/grails-json-apis" ]

    //Support live reloading
    def jsonApiRegistry = new JsonApiRegistry()
    def watchedResources = "file:./grails-app/domain/**.groovy"
    def onChange = { event ->
        //Update ObjectMarshallers
        jsonApiRegistry.updateMarshallers(event.application)
    }
    def doWithApplicationContext = { applicationContext ->
        //Generate and register the required ObjectMarshaller instances.
        jsonApiRegistry.updateMarshallers(application)
    }
}
