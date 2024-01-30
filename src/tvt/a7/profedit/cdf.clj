(ns tvt.a7.profedit.cdf
  (:require [tvt.a7.profedit.profile :as prof]
            [clojure.spec.alpha :as s]
            [instaparse.core :as insta]))


(def drg-grammar
  "<file>        = header radar-data <any>*
   <header>      = <any>* bullet-desc <ignored>+ <newline>
   <bullet-desc> = (<ignored>+ weight-kg) (<ignored>+ diameter-m) (<ignored>+ length-m)
   weight-kg     = number
   diameter-m    = number
   length-m      = number
   radar-data    = (line <newline>)*
   <line>        = number <whitespace> number
   any           = #'.'
   <number>      = #'[0-9.]+'
   <whitespace>  = #'[ \t]+'
   <ignored>     = #'[^0-9.\\r\\n]+'
   <newline>     = #'[\\r\\n]+'")


(def ^:private parser (insta/parser drg-grammar :output-format :hiccup))


(defn- convert-to-inches [meters]
  (* meters 39.3701))

(defn- convert-to-grains [kilograms]
  (* kilograms 15432.3584))


(defn- convert-radar-data-to-map [radar-data-lines]
  (let [pairs (partition 2 radar-data-lines)
        maps (map (fn [[k v]] {:cd (Double/parseDouble k) :ma (Double/parseDouble v)}) pairs)]
    (->> maps
         (sort-by :ma >)
         vec)))


(defn- process-drg-file [file-path]
  (-> file-path
      slurp
      parser))


(defn- process-and-convert [file-path]
  (let [parsed-data (vec (process-drg-file file-path))
        [[_ weight-str] [_ diameter-str] [_ length-str] radar-data-lines] parsed-data
        weight-grains (convert-to-grains (Double/parseDouble weight-str))
        diameter-inches (convert-to-inches (Double/parseDouble diameter-str))
        length-inches (convert-to-inches (Double/parseDouble length-str))
        radar-data-map (convert-radar-data-to-map (rest radar-data-lines))]
    {:weight-grains weight-grains
     :diameter-inches diameter-inches
     :length-inches length-inches
     :radar-data radar-data-map}))


(defn- update-profile-with-conversion
  [profile {:keys [weight-grains diameter-inches length-inches radar-data]}]
  (-> profile
      (assoc :b-weight weight-grains
             :b-diameter diameter-inches
             :b-length length-inches
             :coef-custom radar-data
             :bc-type :custom)))


(defn apply-drg-file-to-state! [*state drg-file-path]
  (swap! *state (fn [state]
                  (let [cdf (process-and-convert drg-file-path)
                        profile (:profile state)
                        new-profile (update-profile-with-conversion profile cdf)]
                    (if (s/valid? ::prof/profile new-profile)
                      (assoc state :profile new-profile)
                      state)))))
