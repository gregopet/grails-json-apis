grails-json-api-variants
========================

## Grails plugin for managing multiple JSON api variants using domain class annotations

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

Several API variants can be easily defined in domain classes. Marking a property with the `JsonApi` annotation but providing no name will
include that property in all APIs:

```groovy
import grails.plugins.jsonapivariants.JsonApi

class User {
	@JsonApi
	String screenName

	@JsonApi('userSettings')
	String email

	@JsonApi(['userSettings', 'detailedInformation'])
	String twitterUsername
}
```

Then in the controller one can call the desired named JsonApi configuration to get only
the fields defined for that API. 

```groovy
JSON.use("userSettings")
render person as JSON
```

It works for collections, too (but be careful not to create circular paths):

```groovy
static hasMany = [
	pets: Pet
]
@JsonApi('detailedInformation')
Set pets
```

In order to mark `belongsTo` properties with the `JsonApi` annotation, declare them explicitly:

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

The selected api configurations work across the whole object graph automatically. The 
datastore identity property is always included in all APIs automatically so for
example if you had forgotten to put any `JsonApi` annotations into the `Pet` class
you would only get a list of IDs.


## Future plans

Find all the corner cases in which the current implementation wouldn't work. Then further
in the future an API documenting script would be nice, producing charts or 
markdown docs from the data contained in the annotation(s).