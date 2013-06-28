<!doctype html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Grails JSON API versioning demo</title>
	<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.no-icons.min.css" rel="stylesheet">
</head>
<body>
	<div role="main" class="container">
		<h1>Grails JSON API versioning demo</h1>
		<p class="lead">
			Same data serialized in different API versions - see the domain class source code
			annotations for definitions of 'detailedInformation' and 'userSettings' APIs.
		</p>
		
		<h2>Default JSON renderer</h2>
		<pre>${vanilla}</pre>
		
		<h2>'deep' JSON renderer (included in Grails)</h2>
		<pre>${deep}</pre>
		
		<h2>detailedInformation API (custom defined API via domain model annotations)</h2>
		<pre>${detailedInformation}</pre>
		
		<h2>Custom defined userSettings API (custom defined API via domain model annotations)</h2>
		<pre>${userSettings}</pre>
	</div>
</body>
</html>