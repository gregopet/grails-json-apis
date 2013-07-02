class BootStrap {
	def init = {servletContext ->
		//some demo data to populate our view..
		def person = new User(screenName:'Nelson', email:'nelson@example.org', twitterUsername: 'nelsonIsGod')
		person.addToPets(name: 'Rover', numberOfLegs: 4, likesTickling: true)
		person.addToPets(name: 'Spidey', numberOfLegs: 8, likesTickling: false)
		person.addToPets(name: 'Venom', numberOfLegs: 0, likesTickling: false)
		person.save(failOnError:true)
	}
}