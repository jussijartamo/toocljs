(do
  ;
  (import toocljs.T)
  (require '[clojure.java.jdbc :as j])
  (require '[clojure.data.json :as json])
  ;
  (defn resolve [kw]
    (T/getProperty (name kw)))

  (defn safe [t]
    (str t))

  (defn safe-all [m]
    (into {} (for [[k v] m] [k (safe v)])))

  (defn prn
    ([a]
     (if (or (vector? a) (seq? a))
       (T/prn (json/write-str (map safe-all a)))
       (if (map? a)
         (T/prn (json/write-str [(safe-all a)]))
         (T/prn (json/write-str [{:result (str a)}]))))))

  (defn db [uri]
    {:connection-uri (if (keyword? uri) (resolve uri) uri)})

  ; Code from front-end comes here!
  %s)
