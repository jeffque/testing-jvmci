package jeff;

import org.graalvm.polyglot.Context;

public class Ackerman {

	private static final String ACKJS = """
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

		function compute(c) {
			let cnt = 0
			while (!c.finished) {
				c = c.step();
				cnt++
				if (cnt % (100*1000) == 0) console.log(cnt)
			}
			return c.value
		}
		console.log(compute(ackermannTrampoline(2, value(12))));
		""";

	private static long runTest(String descricao, Runnable r) {
		final var v = new Ackerman();
		long total = 0;
		for (int i = 0; i < 10; i++) {
			long antes = System.currentTimeMillis();
			r.run();
			long depois = System.currentTimeMillis();
			long delta = depois - antes;
			total += delta;
			System.out.println(descricao + ": tempo decorrido (" + i + "): " + delta);
		}
		return total;
	}

	public static void main(String... args) {
		final var v = new Ackerman();
		//final var tempoJava = runTest("java", () -> System.out.println(v.get(2, 12)));
		//System.out.println("tempo java: " + tempoJava);

		//long total = 0;
		final long tempoJs;
		try (final var context = Context.newBuilder()
			.option("js.nashorn-compat", "true")
                        .option("js.ecmascript-version", "2020")
                        .allowAllAccess(true)
                        .build();) {

			tempoJs = runTest("js", () -> context.eval("js", ACKJS));
		}
		System.out.println("tempo js: " + tempoJs);
	}

	long get(long m, long n) {
		return Continuation.compute(ackermannPeter(m, Continuation.found(n)));
	}

	private static Continuation ackermannPeter(long m, Continuation c) {
	    if (!c.finished()) {
		return Continuation.goon(() -> {
		    final var next = c.step();
		    return Continuation.goon(() -> ackermannPeter(m, next));
		});
	    }
	    long n = c.value();
	    if (m <= 0) {
		return Continuation.found(n + 1);
	    }
	    if (n <= 0) {
		return Continuation.goon(() -> ackermannPeter(m - 1, Continuation.found(1)));
	    }
	    return Continuation.goon(() ->
		ackermannPeter(m - 1,
		    Continuation.goon(() -> ackermannPeter(m, Continuation.found(n - 1)
		)))
	    );
	}

}
