(ns progress.meter
  "Nice console progress meters for sequences.
   Inspired by python's tqdm")

(def bar-width 10)

(def ^:dynamic *default-params*
  {:print-fn #?(:clj print :cljs js/console.log)
   :flush-fn #?(:clj flush :cljs (fn [_]))})

(defn epoch
  []
  (quot #?(:clj  (System/currentTimeMillis)
           :cljs (.now js/Date))
        1000))

(defn hms
  [t]
  (let [s (rem t 60)
        m (quot t 60)
        h (quot m 60)]
    (if (pos? h)
      [h (rem m 60) s]
      [nil m s])))

(defn pad
  [n w]
  #?(:clj  (format (str "%0" w "d") (long n))
     :cljs (let [nstr (str (long n))
                 nlen (count nstr)
                 plen (- w nlen)]
             (if (pos? plen)
               (reduce str (concat (repeat plen "0")
                                   [nstr]))
               nstr))))

(defn format-interval
  [t]
  (let [[h m s] (hms t)]
    (if h
      (str (long h) ":" (pad m 2) ":" (pad s 2))
      (str (pad m 2) ":" (pad s 2)))))

(defn draw-bar
  [length width]
  (reduce str (concat (repeat length "#") (repeat (- width length) "-"))))

(defn format-meter
  ([n total elapsed]
   (if (> n total)
     (format-meter n elapsed)
     (let [frac       (double (/ n total))
           bar-length (long (* frac bar-width))
           bar        (draw-bar bar-length bar-width)
           percentage (str (pad (* 100 frac) 3) "%")]
       (str "|" bar "| " n "/" total " "
            percentage " [elapsed: "
            (format-interval elapsed) "]"))))
  ([n elapsed]
   (str n " [elapsed: " (format-interval elapsed) "]")))

(defn update-meter
  [{:keys [n start-ts last? total last-len print-fn flush-fn] :as params}]
  (let [processed (inc n)
        ts        (epoch)
        elapsed   (- ts start-ts)
        text      (format-meter (inc n) total elapsed)
        len       (count text)
        pad       (reduce str (repeat (- last-len len) " "))]
    (print-fn (str "\r" text pad))
    (when last?
      (print-fn "\n"))
    (flush-fn)
    (assoc params :last-len len :n processed)))

(defn progress-seq
  ([input]
   (let [s (seq input)]
     (progress-seq s (merge *default-params*
                            {:n        0
                             :start-ts (epoch)
                             :total    (count s)
                             :last-len 0}) )))
  ([[head & tail] params]
   (let [params (update-meter (assoc params :last? (nil? tail)))]
     (cons head (when tail (lazy-seq (progress-seq tail params)))))))
