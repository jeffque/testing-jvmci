package jeff;

import java.util.function.Supplier;

public interface Continuation {
    boolean finished();
    long value();
    Continuation step();

    public static long compute(Continuation c) {
	    long cnt = 0;
	    while (!c.finished()) {
		c = c.step();
		cnt++;
		//if (cnt % 10_000_000L == 0) {
		//	System.out.println("cnt " + cnt);
		//}
	    }
	    return c.value();
	}


    // computation has over
    public static Continuation found(long v) {
        return new Continuation() {
            @Override
            public boolean finished() {
                return true;
            }

            @Override
            public long value() {
                return v;
            }

            @Override
            public Continuation step() {
                return this;
            }
        };
    }

    // go on computing
    public static Continuation goon(Supplier<Continuation> nextStep) {
        return new Continuation() {
            @Override
            public boolean finished() {
                return false;
            }

            @Override
            public long value() {
                return 0;
            }

            @Override
            public Continuation step() {
                return nextStep.get();
            }
        };
    }
}

