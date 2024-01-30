(ns tvt.a7.profedit.cdf
  (:require [instaparse.core :as insta]))

(def example
  {:profile
   {:profile-name "Savage 110A"
    :cartridge-name "UKROP 338LM 250GRN"
    :bullet-name "SMK 250GRN HPBT"
    :short-name-top "338LM"
    :short-name-bot "250GRN"
    :caliber "9mm"
    :device-uuid ""
    :user-note "Add your profile specific notes here"
    :zero-x 0.0
    :zero-y 0.0
    :distances [100.0 100.0 120.0 130.0 140.0
                150.0 160.0 170.0 180.0 190.0
                200.0 210.0 220.0 250.0 300.0
                1000.0 1500.0 1600.0 1700.0 2000.0 3000.0]
    :switches [{:c-idx 255
                :distance-from :value
                :distance 100.0
                :reticle-idx 0
                :zoom 1}
               {:c-idx 255
                :distance-from :value
                :distance 200.0
                :reticle-idx 0
                :zoom 2}
               {:c-idx 255
                :distance-from :value
                :distance 300.0
                :reticle-idx 0
                :zoom 3}
               {:c-idx 255
                :distance-from :value
                :distance 1000.0
                :reticle-idx 0
                :zoom 4}]
    :sc-height 90.0
    :r-twist 9.45
    :twist-dir :right
    :c-muzzle-velocity 890.0
    :c-zero-temperature 25.0
    :c-t-coeff 1.03
    :c-zero-distance-idx 0
    :c-zero-air-temperature 15.0
    :c-zero-air-pressure 1000.0
    :c-zero-air-humidity 40.0
    :c-zero-w-pitch 0.0
    :c-zero-p-temperature 15.0
    :b-diameter 0.338
    :b-weight 250.0
    :b-length 1.55
    :coef-g1 [{:bc 0.343 :mv 850.0}
              {:bc 0.335 :mv 600.0}
              {:bc 0.325 :mv 400.0}]
    :coef-g7 [{:bc 0.343 :mv 850.0}]
    :coef-custom [{:cd 0.8 :ma 1.0}
                  {:cd 0.3 :ma 0.6}
                  {:cd 0.1 :ma 0.4}]
    :bc-type :g1}})


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

(def parser (insta/parser drg-grammar :output-format :hiccup))

(defn convert-to-inches [meters]
  (* meters 39.3701))

(defn convert-to-grains [kilograms]
  (* kilograms 15432.3584))

(defn convert-radar-data-to-map [radar-data-lines]
  (let [pairs (partition 2 radar-data-lines)
        maps (map (fn [[k v]] {:cd (Double/parseDouble k) :ma (Double/parseDouble v)}) pairs)]
    (->> maps
         (sort-by :ma >)
         vec)))

(defn process-drg-file [file-path]
  (-> file-path
      slurp
      parser))

(defn process-and-convert [file-path]
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


(defn update-profile-with-conversion
  [profile {:keys [weight-grains diameter-inches length-inches radar-data]}]
  (-> profile
      (assoc-in [:profile :b-weight] weight-grains)
      (assoc-in [:profile :b-diameter] diameter-inches)
      (assoc-in [:profile :b-length] length-inches)
      (assoc-in [:profile :coef-custom] radar-data)
      (assoc-in [:profile :bc-type] :custom)))


;; Usage example
(update-profile-with-conversion example (process-and-convert "/tmp/lapua_ballistics/1.drg"))
