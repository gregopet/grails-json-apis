import grails.converters.JSON

class DemoController {
	//For some reason that last call to JSON.use("detailedInformation")
	//gets 'stuck' between requests which causes the first JSON 
	//serialization to not use the vanilla converter at all.
	//Hence the explicit scope=prototype call, which sadly doesn't
	//help at all but may help me remember my line of reasoning.
	static scope = "prototype"
	
	def index() {
		def demoData = User.first()
		def model = [:]
		
		//serialize a couple of API variants into the model..
		model.vanilla = new JSON(demoData).toString(true)
		
		JSON.use("deep")
		model.deep = new JSON(demoData).toString(true)
		
		JSON.use("userSettings")
		model.userSettings = new JSON(demoData).toString(true)
		
		JSON.use("detailedInformation")
		model.detailedInformation = new JSON(demoData).toString(true)
		
		return model
	}

}