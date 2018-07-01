# clatchet

A toy implementation of the Double Ratchet protocol.

## Development Mode

### Run application:

```
lein dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build


To compile clojurescript to javascript:

```
lein build
```

## TODO

 * error handling in `clatchet.crypto`
 * spec
 * transmission of PK and ciphertext together
 * authentication
