(ns tvt.a7.profedit.cdf
  (:require [instaparse.core :as insta]))

(def drg-grammar
  "<file>        = header radar-data <ignored>*
   <header>      = (<ignored>|<number>)* bullet-desc <ignored>+ <newline>
   <bullet-desc> = (<ignored>+ weight-kg) (<ignored>+ diameter-m) (<ignored>+ length-m)
   weight-kg     = number
   diameter-m    = number
   length-m      = number
   radar-data    = (line <newline>)*
   line          = number <whitespace> number
   <number>      = #'[0-9.]+'
   <whitespace>  = #'[ \t]+'
   <ignored>     = #'[^0-9.\\r\\n]+'
   <newline>     = #'[\\r\\n]+'")


(def parser (insta/parser drg-grammar :output-format :hiccup))


(defn process-drg-file [file-path]
  (-> file-path
      slurp
      parser))

(defn convert-to-inches [meters]
  (* meters 39.3701))

(defn convert-to-grains [kilograms]
  (* kilograms 15432.3584))

(defn process-line [m tag val]
  (cond
    (= tag :radar-data) (update m :radar-data conj val)
    (= tag :weight-kg) (assoc m :weight-grains
                              (convert-to-grains (Double/parseDouble val)))
    (= tag :diameter-m) (assoc m :diameter-inches
                               (convert-to-inches (Double/parseDouble val)))
    (= tag :length-m) (assoc m :length-inches
                             (convert-to-inches (Double/parseDouble val)))
    :else m))

(defn hiccup-to-map [hiccup-data]
  (let [initial-map {:weight-grains nil :diameter-inches nil
                     :length-inches nil :radar-data []}]
    (reduce (fn [m [tag & [val]]] (process-line m tag val))
            initial-map
            hiccup-data)))

(defn process-and-convert [file-path]
  (->> file-path
       process-drg-file
       (filter vector?)
       (map #(vector (first %) (second %)))
       hiccup-to-map))

(process-and-convert "/tmp/lapua_ballistics/1.drg")
