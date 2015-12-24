# progress: ASCII sequence progress meters

This is essentialy python tqdm in clojure.

## Usage

```clojure
[[spootnik/progress "0.1.3"]]
```

```clojure
(doseq [i (progress-seq (range 20))]
  (Thread/sleep 200))
```

This outputs in a repl:

```
user=> (doseq [i (progress-seq (range 20))] (Thread/sleep 200))
|######----| 13/20  65% [elapsed: 00:02]
```


## License

Copyright © 2015 Pierre-Yves Ritschard <pyr@spootnik.org>

Distributed under the MIT License.
