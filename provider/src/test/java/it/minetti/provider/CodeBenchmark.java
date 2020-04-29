package it.minetti.provider;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Fork(1)
@Warmup(iterations = 2)
@Measurement(iterations = 10)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CodeBenchmark {

    @Benchmark
    public BigInteger benchmark() {
        return new BigInteger(500, 9, new Random());
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(CodeBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

}
