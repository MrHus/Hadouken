## What is Hadouken

Hadouken is a clojure template system. What it aim's to do is take a file
and extract all the clojure expressions and replace them with their values.

## Example 1: HTML

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

## Example 2: CSS

//css.tpl
.someclass
{
    color:  (str black);
}

ul
{
    width: (/ height 2);
}

Calling (template "css.tpl" {'black "#000000", 'height 1024}) will print:

`.someclass
{
    color:  #000000;
}

ul
{
    width: 512;
}` 

## How does it work

Behind the scenes its all just an elaborate format function.
First the file gets read into a string.
Second all the expressions from the string are gathered in a sequence.
Third they get evaluated into a sequence of values.
Fourth all the expressions in the string get replaced with %s
Fifth and finally format is called with the string and the values as arguments.

The evaluation of these expressions are done inside a temporary namespace. So no outside
variables can get in. Forcing you to put all the variables inside of a hashmap as an argument.
The every key value pair gets def'd inside the temporary namespace.

## Warning

This stuff uses eval so its probably not safe for user inputted data, but its on my todo list.

## Todo

Get the [walton](http://github.com/defn/walton/blob/master/src/walton/core.clj#L38) library working so extract-expressions can get imported, and to credit the writer.