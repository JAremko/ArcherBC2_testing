(ns tvt.a7.profedit.cdf
  (:require [instaparse.core :as insta]))

(def drg-grammar
  "<file>       = header radar-data <ignored>*
   <header>     = (<ignored>|<number>)* bullet-desc <ignored>+ <newline>
   <bullet-desc>  = (<ignored>+ weight-kg) (<ignored>+ diameter-m) (<ignored>+ length-m)
   weight-kg    = number
   diameter-m   = number
   length-m     = number
   radar-data   = (line <newline>)*
   <line>       = number <whitespace> number
   <number>     = #'[0-9.]+'
   <whitespace> = #'[ \t]+'
   <ignored>    = #'[^0-9.\\r\\n]+'
   <newline>    = #'[\\r\\n]+'")


(def parser (insta/parser drg-grammar))


(defn process-drg-file [file-path]
  (-> file-path
      slurp
      parser))


(process-drg-file "/tmp/lapua_ballistics/1.drg")
