json-apis
========================

## Grails plugin for managing multiple JSON apis using domain class annotations

The goal of this plugin is to help convert Grails domain classes into various
JSON representations needed in different parts of your web application or to 
support various API versions. 

Features:

 - Allows you to declare multiple named JSON configurations for different use cases
 - Configuration is very straightforward: all that is required is to mark domain class
   properties with a single annotation naming the configurations under which that 
   property should be included in the serialized JSON object
 - Works for collections as well as `belongsTo` properties
 - Enables developers to avoid the circular object reference problem elegantly by
   defining appopriate namespaces - this way it is possible to start the serialization
   either in a parent or child entity, depending on the use case
 - Works for objects serialized inside a [JSON builder](http://grails.org/doc/latest/guide/theWebLayer.html#moreOnJSONBuilder)
 - Uses the Grails' `ObjectMarshaller` mechanism under the hood

## Example of use

Several API variants can be easily defined in domain classes by annotating properties with
`JsonApi` and providing a list of API profile names under which that property should appear in the
resulting JSON. Marking a property with the `JsonApi` annotation but providing no API names will
include that property in all APIs. The database identity property will always be included
automatically. One could for instance define the following domain class:

```groovy
import grails.plugins.jsonapis.JsonApi

class User {
	@JsonApi
	String screenName

	@JsonApi('userSettings')
	String email

	@JsonApi(['userSettings', 'detailedInformation'])
	String twitterUsername
}
```

Then in the controller one would call the desired named JsonApi configuration to get only
the fields defined for that API. The following code:

```groovy
JSON.use("detailedInformation")
render person as JSON
```

...would convert the `person` object into JSON containing the `id`, `screenName` and `twitterUsername`
properties but not the `email`. It works for collections as well, converting each collection
member using the same API profile that was used to convert the parent:

```groovy
static hasMany = [
	pets: Pet
]
@JsonApi('detailedInformation')
Set pets
```

To include a domain object's parent in a JSON API, declare a `belongsTo` property explicitly
and annotate it with `JsonApi` (but be careful not to create circular paths by including both
ends of a `belongsTo`/`hasMany` pair):

```groovy
static belongsTo = [
	user:User
]

@JsonApi('petDetails') 
User user
```

JSONBuilder is supported, too:

```groovy
JSON.use("userSettings")
render(contentType: "text/json") {
    user = User.first()
    pet = Pet.first()
}
```


## Future plans

 - Detect circular APIs and display a warning on startup, perhaps disable them entirely
 - Detect API changes without restarting the app in development mode
 - Add a script/controller that would document the registered APIs in one or more formats
 - Read the domain class annotations and produce configurations for those 3rd party JSON 
   renderers which currently seem to perform better than the native Grails implementation