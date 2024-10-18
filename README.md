Testing GraalVM's speedup against Ackermann Peter function.

# The testing

I have used Ackermann Peter function, using trampoline
to avoid stack overflow errors.

In JS, this is the function being tested:

```js
function ackermannPeter(m, n) {
    if (m === 0) {
        return n + 1;
    } if (n === 0) {
        return ackermannPeter(m-1, 1);
    }
    return ackermannPeter(m-1, ackermannPeter(m, n-1));
}

function goon(s) {
    return ({
        finished: false,
        step: s,
    })
}

function value(v) {
    return ({
        finished: true,
        value: v,
    })
}

function compute(c) {
    let cnt = 0
    while (!c.finished) {
        c = c.step();
        cnt++
        if (cnt % (100*1000) == 0) console.log(cnt)
    }
    return c.value
}

function ackermannTrampoline(m, c) {
    //console.log(`m ${m}, c.fished ${c.finished}`)
    if (!c.finished) {
        return goon(() => {
            const next = c.step();
            return goon(() => ackermannTrampoline(m, next));
        })
    }
    const n = c.value;

    if (m === 0) {
        return value(n + 1);
    } if (n === 0) {
        return goon(() => ackermannTrampoline(m-1, value(1)));
    }
    return goon(() => ackermannTrampoline(m-1, goon(() => ackermannTrampoline(m, value(n-1)))));
}

```

The results were doing against those JDKs (installed via SDKMan):

- `21.0.3-tem`
- `21.0.2-graalce`

The tests used Maven `3.9.9`, installed via SDKMan.

With this configuration, running to compute the Ackermann Peter function
with args `(2, 12)` the following results were obtained:


| JDK   | Operations | Time (in seconds) |
| ----  | ---------- | ----------------: |
| GraalVM, JVMCI enabled | ~270M | 16 |
| GrralVM, JVMCI disabled | ~270M | 244 |
| Temurin | ~270M | 239 |

