package grails.plugins.jsonapis

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention( RetentionPolicy.RUNTIME )
public @interface JsonApi {
	String[] value() default []
}
