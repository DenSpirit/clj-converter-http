(ns converter-http.core
  (:require [org.httpkit.server :refer [run-server]]))

(def convert-pattern #"/Convert\(\"([-+]?\d+)\", ?\"(\w+)\", ?\"(\w+)\"\)")

; singleton pattern
(def convert-map {
  ; temperatures
  :K->C #(- % 273.15)
  :K->F #(- (* % 9/5) 459.67)
  :C->K #(+ % 273.15)
  :C->F #(+ (* % 9/5) 32)
  :F->C #(* (- % 32) 5/9)
  :F->K #(* (+ % 459.67) 5/9)
  ; masses
  :LB->PUD #(* % 40)
  :LB->KG #(* % 2.20462)
  :PUD->LB #(/ % 40)
  :PUD->KG #(/ % 16.3804964)
  :KG->PUD #(* % 16.3804964)
  :KG->LB #(* % 0.453592)
  ; distanses
  :MI->M #(* % 1609.34)
  :MI->VER #((convert-map :M->VER) ((convert-map :MI->M) %))
  :M->MI #(/ % 1609.34)
  :M->VER #(/ % 1066.8)
  :VER->M #(* % 1066.8)
  :VER->MI #((convert-map :VER->M) ((convert-map :M->MI) %))
})

(defn to-keyword [from to] (keyword (clojure.string/upper-case (str from "->" to))))

; factory pattern
(defn get-converter [from to] (if (= from to) identity (convert-map (to-keyword from to) nil)))

(defn app [req] 
  (if-let [[_ value from to] (re-matches convert-pattern (:uri req))]
    ; strategy pattern
    (if-let [converter (get-converter from to)]
      { :status 200 :body (str (converter (Float/parseFloat value)) "\n") }
      { :status 400 :body "Could not convert\n" })
    { :status 404 }))

(defn -main [& args]
  (run-server app { :port 8000 })
  (println "running"))
