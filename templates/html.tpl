<html>
    <head>
        <title>(:name person)</title>
        <body>
            <h1>(* 3 (+ 1 2) )</h1>
            <p>(str "Whats happening with you")<p>
            <p>(:name person)</p>
            <ul>
                (apply str (for [i (range 1 10)] (str "<li>The number is: " i "</li>")))
            </ul>        
        </body>
    </head>
</html>