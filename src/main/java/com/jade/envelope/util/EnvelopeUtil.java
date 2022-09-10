package com.jade.envelope.util;

import java.util.Random;

public class EnvelopeUtil {

    static Random random = new Random();

    static {
        random.setSeed(System.currentTimeMillis());
    }

    /**
     * 生产min和max之间的随机数，但是概率不是平均的，从min到max方向概率逐渐加大。
     * 先平方，然后产生一个平方值范围内的随机数，再开方，这样就产生了一种“膨胀”再“收缩”的效果。
     */
    static long xRandom(long min, long max) {
        return sqrt(nextLong(sqr(max - min)));
    }

    public static long[] generate(long account, int num) {
        long[] result = new long[num];

        long max = account;
        long min = 0;
        long average = account / num;

        for (int i = 0; i < result.length; i++) {
            //当随机数>平均值，则产生小红包
            //当随机数<平均值，则产生大红包
            if (nextLong(min, max) > average) {
                long temp = min + xRandom(min, average);
                result[i] = temp;
                account -= temp;
            } else {
                long temp = max - xRandom(average, max);
                result[i] = temp;
                account -= temp;
            }
        }
        // 如果还有余钱，则尝试加到小红包里，如果加不进去，则尝试下一个。
        while (account > 0) {
            for (int i = 0; i < result.length; i++) {
                if (account > 0 && result[i] < max) {
                    result[i]++;
                    account--;
                }
            }
        }
        // 如果钱是负数了，还得从已生成的小红包中抽取回来
        while (account < 0) {
            for (int i = 0; i < result.length; i++) {
                if (account < 0 && result[i] > min) {
                    result[i]--;
                    account++;
                }
            }
        }
        return result;
    }

    static long sqrt(long n) {
        // 改进为查表？
        return (long) Math.sqrt(n);
    }

    static long sqr(long n) {
        // 查表快，还是直接算快？
        return n * n;
    }

    static long nextLong(long n) {
        return random.nextInt((int) n);
    }

    static long nextLong(long min, long max) {
        return random.nextInt((int) (max - min + 1)) + min;
    }
}
