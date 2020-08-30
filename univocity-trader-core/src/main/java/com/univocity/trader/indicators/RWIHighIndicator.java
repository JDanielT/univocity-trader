package com.univocity.trader.indicators;

import com.univocity.trader.candles.Candle;
import com.univocity.trader.indicators.base.MultiValueIndicator;
import com.univocity.trader.indicators.base.TimeInterval;
import com.univocity.trader.strategy.Indicator;
import com.univocity.trader.utils.CircularList;

import java.util.function.ToDoubleFunction;

public class RWIHighIndicator extends MultiValueIndicator {

    private double value;
    private AverageTrueRange atrIndicator;

    private CircularList lowList;

    public RWIHighIndicator(TimeInterval interval) {
        this(20, interval);
    }

    public RWIHighIndicator(int length, TimeInterval interval) {
        this(length, interval, c -> c.close);
    }

    public RWIHighIndicator(int length, TimeInterval interval, ToDoubleFunction<Candle> valueGetter) {
        super(length, interval, valueGetter);
        lowList = new CircularList(length);
        atrIndicator = new AverageTrueRange(length, interval);
    }

    @Override
    protected boolean calculateIndicatorValue(Candle candle, double value, boolean updating) {
        if (atrIndicator.accumulate(candle)) {
            lowList.accumulate(candle.low, updating);

            if (values.size() < values.capacity()) {
                this.value = Double.NaN;
                return true;
            }

            double maxRWIH = 0;
            for (int n = 2; n <= values.capacity(); n++) {
                maxRWIH = Math.max(maxRWIH, calcRWIHFor(candle, n));
            }

            this.value = maxRWIH;
            return true;

        }
        return false;
    }

    @Override
    protected Indicator[] children() {
        return new Indicator[]{atrIndicator};
    }

    private double calcRWIHFor(final Candle candle, final int n) {
        double high = candle.high;
        double lowN = lowList.get(values.size() - n);
        double atrN = atrIndicator.getValue();
        double sqrtN = Math.sqrt(n);

        return (high - lowN) / (atrN * sqrtN);
    }

    @Override
    public double getValue() {
        return value;
    }

}
