(ns hadouken.core-test
  (:use [hadouken.core] :reload-all)
  (:use [clojure.test]))

(deftest eval-str-test
  (is (= 5 (eval-str "(+ 2 3)" *ns*)))
  (is (= "henk" (eval-str "(str \"henk\")" *ns*)))) 

(deftest parse-template-test
  (is (= "Hello %s how are you doing, %s thank you." (parse-template "Hello (str \"Maarten\") how are you doing, (+ 10 10) thank you." ["(str \"Maarten\")" "(+ 10 10)"])))
  (is (= "%s %s %s" (parse-template "(+ 10 10) (- 10 (- 5 3)) (+ 7 (- 7 (+ 4 4)))" ["(+ 10 10)" "(- 10 (- 5 3))" "(+ 7 (- 7 (+ 4 4)))"]))))
  
(deftest template-test
  (is (= "<html>\n    <head>\n        <title>Maarten Hus</title>\n        <body>\n            <h1>9</h1>\n            <p>Whats happening with you<p>\n            <p>Maarten Hus</p>\n            <ul>\n                <li>The number is: 1</li><li>The number is: 2</li><li>The number is: 3</li><li>The number is: 4</li><li>The number is: 5</li><li>The number is: 6</li><li>The number is: 7</li><li>The number is: 8</li><li>The number is: 9</li>\n            </ul>        \n        </body>\n    </head>\n</html>" 
       (template "templates/html.tpl" {'person {:name "Maarten Hus"}})))
  (is (= ".someclass\n{\n    color:  #000000;\n}\n\nul\n{\n    width: 512;\n}"
       (template "templates/css.tpl" {'black "#000000", 'height 1024}))))         