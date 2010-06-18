# What is Hadouken

Hadouken is a clojure template system. What it aim's to do is take a file
and extract all the clojure expressions and replace them with their values.

## Example: generating HTML

We have file: test.tpl

// test.tpl
<html>
	<head>
		<title>
			<body>
				<p>(+ 1 2)</p>
				<p>(:name person)</p>
			</body>
		</title>
	</head>
</html>

Then you call (print (template "test.tpl" {'person {:name "Fred Ethel"}}))
Which returns:

<html>
	<head>
		<title>
			<body>
				<p>3</p>
				<p>Fred Ethel</p>
			</body>
		</title>
	</head>
</html>

### How does it work

Behind the scenes its all just an elaborate format function.
First the file gets read into a string.
Second all the expressions from the string are gathered in a sequence.
Third they get evaluated into a sequence of values.
Fourth all the expressions in the string get replaced with %s
Fifth and finally format is called with the string and the values as arguments.

The evaluation of these expressions are done inside a temporary namespace. So no outside
variables can get in. Forcing you to put all the variables inside of a hashmap as an argument.
The every key value pair gets def'd inside the temporary namespace.

## Cache

You can also cache templates with the wrapper function cache. Cache is used just like the template function.
The only difference being that it saves the compiled version to the *template-dir* directory. The second argument
alive-for is the amount of hours that the template is valid.

(cache "test.tpl" 2 {'person {:name "Fred Ethel"}})) means parse template "test.tpl" and keep it alive for 2 hours.

### How it works

Its pretty simple, everytime a call is made to parse a template with the cache function. It checks if that file exists
by file name inside of a vector of (defstruct templ :file :created-on :delete-on). If it does not exits it creates a record
of it and adds it to the vector, and returns the parsed template. If a recored of the file is found the time gets checked to 
see if its still valid. If its valid, just return the compiled file from the cache, if not just parse and create a new record. 

## Warning

This stuff uses eval so its probably not safe for user inputted data, but its on my todo list.

## Todo

Get the [walton](http://github.com/defn/walton/blob/master/src/walton/core.clj#L38) library working so extract-expressions can get imported, and to credit the writer.