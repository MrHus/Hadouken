(ns hadouken.core
  (:use [clojure.contrib.duck-streams :only (slurp*)]))

;; Stole from with-ns and clj-sandbox    
(defn map-to-ns
  "Take a hashmap and create a namespace from the key value pairs.
  {age 39 underage false} -> (def name 39) (def underage false)"
  [m]
  (if (empty? m)
    (create-ns (gensym "hadouken.core.tns"))
    (let [n (gensym "hadouken.core.tns")] 
      (binding [*read-eval* false *ns* (create-ns n)]
        (refer 'clojure.core)
        (doseq [[k v] m] (eval `(def ~k ~v)))
        *ns*))))     

(defn eval-str
  "Eval a string within the templates n"
  [s #^Namespace n]
  (binding [*ns* n]
    (eval (read-string s))))

;; http://github.com/defn/walton/blob/master/src/walton/core.clj#L38
;; Had trouble getting it via dependencies because the build failed.
;; Need to credit see link.
(defn extract-expressions
  "Extracts sexps."
  [string]
  (second
   (reduce (fn [[exp exps state cnt] c]
             (cond
              (= state :escape)
              [(.append exp c) exps :string cnt]
              (= state :string) (cond
                                 (= c \")
                                 [(.append exp c) exps :code cnt]
                                 (= c \\)
                                 [(.append exp c) exps :escape cnt]
                                 (= c \\)
                                 [(.append exp c) exps :escape cnt]
                                 :else
                                 [(.append exp c) exps :string cnt])
              (and (= cnt 1) (= c \)))
              [(java.lang.StringBuilder.) (cons (str (.append exp c)) exps) :text 0]
              (= c \()
              [(.append exp c) exps :code (inc cnt)]
              (and (> cnt 1) (= c \)))
              [(.append exp c) exps :code (dec cnt)]
              (and (> cnt 0) (= c \"))
              [(.append exp c) exps :string cnt]
              (> cnt 0)
              [(.append exp c) exps :code cnt]
              :else [exp exps state cnt]))
           [(java.lang.StringBuilder.) '() :text 0]
           string)))

(defn parse-template
  [tpl exprs]
  (reduce #(.replace %1 %2 "%s") tpl exprs))

(defn template 
  "Fill the template with the values"
  [template args]
  (let [utpl   (slurp* template) ; get unparsed template
        tns    (map-to-ns args)
        exprs  (reverse (extract-expressions utpl))
        values (for [s exprs] (eval-str s tns))
        ptpl   (parse-template utpl exprs)]; parse template
      (eval `(format ~ptpl ~@values))))

;;(template "templates/html.tpl" {'person {:name "Maarten Hus"}})
;;(template "templates/css.tpl" {'black "#000000", 'height 1024})