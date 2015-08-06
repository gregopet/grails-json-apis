package grails.plugins.jsonapis

/**
 * Builds a graph of objects contained in individual declared APIs.
 */
class ApiGraph {
	
	/** 
	 * Returns a Set containing all root AnnotationMarshallers for 
	 * a named API profile.
	 */
	static Set generate(List<AnnotationMarshaller> marshallers) {
		def nonTrivialMarshallers = marshallers.findAll { it.propertiesToSerialize }
		def topLevelMarshallers = nonTrivialMarshallers as Set
		
		for (marshaller in nonTrivialMarshallers) {
			for (prop in marshaller.propertiesToSerialize) {
				//bidirectional, hasOne, manyToMany,oneToMany, manyToOne
				if (prop.bidirectional || prop.hasOne || prop.manyToMany || prop.oneToMany || prop.manyToOne) {
					def referencedMarshaller = nonTrivialMarshallers.find { it.forClass == prop.referencedDomainClass }
					if (referencedMarshaller) topLevelMarshallers.remove(referencedMarshaller)
				}
			}
		}
		
		return topLevelMarshallers
	}
}