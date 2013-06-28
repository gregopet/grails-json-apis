grails-json-api-variants
========================
*(this project is still in alpha stage and may never leave it - you've
been warned!)*

## Grails plugin for managing multiple JSON api variants using domain class annotations

The goal of this plugin is to help convert Grails domain classes into various
JSON representations needed in different parts of your web application or to 
support various API versions. It avoids circular object graph problems (by relaying
that responsibility to you, ha!) and does not require developers to write `Map`
producing`toAPI` methods of various complexities. Under the hood it uses the Grails
`ObjectMarshaller` mechanism.

## Example of use

Several API variants can be easily defined in domain classes:

```groovy
@Api('userSettings')
String email

@Api(['userSettings', 'detailedInformation'])
String twitterUsername
```

Then in the controller one can call the desired named Api configuration to get only
the fields defined for that API:

```groovy
JSON.use("userSettings")
render person as JSON
```

Marking a property with the `Api` annotation but providing no API name will
include that property in all APIs. It works for collections, too (but be careful
not to create circular paths):

```groovy
static hasMany = [
	pets: Pet
]
@Api('detailedInformation')
Set pets
```

Api configurations work across the whole object graph automatically so in the above
example any `Pet` domain property not annotated to appear in the `detailedInformation`
configuration wouldn't be rendered. The datastore identity property is always included
in all APIs automatically so if you had forgotten to put any `Api` annotations into the
`Pet` class you would only get a list of IDs.

## Demo

You can clone this repository and `run-app` it or just browse the domain classcode on
Github and check the output of the single action [here](https://rawgithub.com/gregopet/grails-json-api-variants/master/demo-output.html).

## Future plans

As this plugin was the result of a single hacking session the first plan is to look at the code
with fresh eyes. Then write some unit tests :) And after that to find all the corner cases in
which the current implementation wouldn't work - the only limitation I am currently aware
of is that non automatically generated getters and transient properties don't yet work,
but I am sure there will be others.

Then further in the future an API documenting script would be nice, producing charts or 
markdown docs from the data contained in the annotation(s).