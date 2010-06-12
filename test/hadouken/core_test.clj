(ns hadouken.core-test
  (:use [hadouken.core] :reload-all)
  (:use [clojure.test]))

(deftest eval-str-test
  (is (= 5 (eval-str "(+ 2 3)" *ns*)))
  (is (= "henk" (eval-str "(str \"henk\")" *ns*)))) 

(deftest parse-template-test
  (is (= "Hello %s how are you doing, %s thank you." (parse-template "Hello (str \"Maarten\") how are you doing, (+ 10 10) thank you." ["(str \"Maarten\")" "(+ 10 10)"])))
  (is (= "%s %s %s" (parse-template "(+ 10 10) (- 10 (- 5 3)) (+ 7 (- 7 (+ 4 4)))" ["(+ 10 10)" "(- 10 (- 5 3))" "(+ 7 (- 7 (+ 4 4)))"]))))