(ns hadouken.core
  (:use [clojure.contrib.duck-streams :only (slurp* spit)])
  (:use [clj-time.core]))

(def *template-dir* "templates/")
(def *cache-dir* "cache/")  

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
  [file args]
  (let [utpl   (slurp* (str *template-dir* file)) ; get unparsed template
        tns    (map-to-ns args)
        exprs  (reverse (extract-expressions utpl))
        values (for [s exprs] (eval-str s tns))
        ptpl   (parse-template utpl exprs)]; parse template
      (eval `(format ~ptpl ~@values))))

(def cachebox (ref []))
(defstruct templ :file :created-on :delete-on)

(defn get-from-cache
  "Get all templ structs from the cache that match the :file argument."
  [file]
  (filter #(= (str *cache-dir* file) (:file %)) @cachebox))

(defn add-to-cache
  "Add a templ to the cache"
  [file alive-for]
  (dosync
    (alter cachebox conj (struct templ file (now) (plus (now) (hours alive-for))))))   

(defn clean-cache
  "Remove files that are past their delete-on DateTime."
  []
  (filter #(before? (now) (:delete-on %)) @cachebox))

(defn cache 
  "A specialized template function that cache's results for n amount of hours."
  [file alive-for args]
  (do
    (dosync
      (ref-set cachebox (clean-cache)))  
    (let [matches (get-from-cache file)]
      (if (not (empty? matches))
        (slurp* (:file (first matches)))
        (do
          (add-to-cache (str *cache-dir* file) alive-for)
          (let [tpl (template file args)]
            (spit (str *cache-dir* file) tpl)
            tpl)))))) 
    
;;(template "html.tpl" {'person {:name "Maarten Hus"}})
;;(template "css.tpl" {'black "#000000", 'height 1024})