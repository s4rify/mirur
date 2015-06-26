package mirur.plugin.statsview;

import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import mirur.core.AbstractArrayElementVisitor;
import mirur.core.ArrayElementVisitor;

public interface ArrayStatisticVisitor extends ArrayElementVisitor {
    String getName();

    String getStatistic();

    abstract class AbstractStatisticVisitor extends AbstractArrayElementVisitor implements ArrayStatisticVisitor {
        private final String name;

        protected boolean isValid;

        public AbstractStatisticVisitor(String name) {
            this.name = name;
            isValid = false;
        }

        static boolean isFinite(double v) {
            return !(isNaN(v) || isInfinite(v));
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getStatistic() {
            if (isValid) {
                return getValue();
            } else {
                return "n/a";
            }
        }

        protected abstract String getValue();
    }

    public static class Size extends AbstractStatisticVisitor {
        int count = 0;

        public Size() {
            super("size");
        }

        @Override
        public void visit(double v) {
            isValid = true;
            count++;
        }

        @Override
        protected String getValue() {
            return String.valueOf(count);
        }
    }

    public static class Sum extends AbstractStatisticVisitor {
        double sum = 0;

        public Sum() {
            super("sum");
        }

        @Override
        public void visit(double v) {
            if (isFinite(v)) {
                sum += v;
                isValid = true;
            }
        }

        @Override
        protected String getValue() {
            return String.valueOf(sum);
        }
    }

    public static class Mean extends AbstractStatisticVisitor {
        org.apache.commons.math3.stat.descriptive.moment.Mean mean = new org.apache.commons.math3.stat.descriptive.moment.Mean();

        public Mean() {
            super("mean");
        }

        @Override
        public void visit(double v) {
            if (isFinite(v)) {
                mean.increment(v);
                isValid = true;
            }
        }

        @Override
        protected String getValue() {
            return String.valueOf(mean.getResult());
        }
    }

    public static class Variance extends AbstractStatisticVisitor {
        org.apache.commons.math3.stat.descriptive.moment.Variance var = new org.apache.commons.math3.stat.descriptive.moment.Variance();

        public Variance() {
            super("variance");
        }

        @Override
        public void visit(double v) {
            if (isFinite(v)) {
                var.increment(v);
                isValid = true;
            }
        }

        @Override
        protected String getValue() {
            return String.valueOf(var.getResult());
        }
    }

    public static class Max extends AbstractStatisticVisitor {
        double max = Double.NEGATIVE_INFINITY;
        double finiteMax = Double.NEGATIVE_INFINITY;

        public Max() {
            super("max");
        }

        @Override
        public void visit(double v) {
            if (isFinite(v) && finiteMax < v) {
                finiteMax = v;
            }

            if (max < v) {
                max = v;
                isValid = true;
            }
        }

        @Override
        protected String getValue() {
            if (max == finiteMax) {
                return String.valueOf(max);
            } else {
                return String.valueOf(max) + " (finite max is " + String.valueOf(finiteMax) + ")";
            }
        }
    }

    public static class Min extends AbstractStatisticVisitor {
        double min = Double.POSITIVE_INFINITY;
        double finiteMin = Double.POSITIVE_INFINITY;

        public Min() {
            super("min");
        }

        @Override
        public void visit(double v) {
            if (isFinite(v) && v < finiteMin) {
                finiteMin = v;
            }

            if (v < min) {
                min = v;
                isValid = true;
            }
        }

        @Override
        protected String getValue() {
            if (min == finiteMin) {
                return String.valueOf(min);
            } else {
                return String.valueOf(min) + " (finite min is " + String.valueOf(finiteMin) + ")";
            }
        }
    }

    public static class CountNaN extends AbstractStatisticVisitor {
        int count;

        public CountNaN() {
            super("count of NaN");
        }

        @Override
        public void visit(double v) {
            if (isNaN(v)) {
                count++;
            }
            isValid = true;
        }

        @Override
        protected String getValue() {
            return String.valueOf(count);
        }
    }

    public static class CountPosInf extends AbstractStatisticVisitor {
        int count;

        public CountPosInf() {
            super("count of +Inf");
        }

        @Override
        public void visit(double v) {
            if (v == Double.POSITIVE_INFINITY) {
                count++;
            }
            isValid = true;
        }

        @Override
        protected String getValue() {
            return String.valueOf(count);
        }
    }

    public static class CountNegInf extends AbstractStatisticVisitor {
        int count;

        public CountNegInf() {
            super("count of -Inf");
        }

        @Override
        public void visit(double v) {
            if (v == Double.NEGATIVE_INFINITY) {
                count++;
            }
            isValid = true;
        }

        @Override
        protected String getValue() {
            return String.valueOf(count);
        }
    }

    public static class CountPositive extends AbstractStatisticVisitor {
        int count;

        public CountPositive() {
            super("count of positive");
        }

        @Override
        protected String getValue() {
            return Integer.toString(count);
        }

        @Override
        public void visit(double v) {
            if (v > 0) {
                count++;
            }
            isValid = true;
        }
    }

    public static class CountNegative extends AbstractStatisticVisitor {
        int count;

        public CountNegative() {
            super("count of negative");
        }

        @Override
        protected String getValue() {
            return Integer.toString(count);
        }

        @Override
        public void visit(double v) {
            if (v < 0) {
                count++;
            }
            isValid = true;
        }
    }

    public static class CountZero extends AbstractStatisticVisitor {
        int count;

        public CountZero() {
            super("count of zeros");
        }

        @Override
        protected String getValue() {
            return Integer.toString(count);
        }

        @Override
        public void visit(double v) {
            if (v == 0) {
                count++;
            }
            isValid = true;
        }
    }

    public static class CountTrue extends AbstractArrayElementVisitor implements ArrayStatisticVisitor {
        int count;

        @Override
        public void visit(boolean v) {
            count += v ? 1 : 0;
        }

        @Override
        public void visit(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return "count of true";
        }

        @Override
        public String getStatistic() {
            return String.valueOf(count);
        }
    }

    public static class CountFalse extends AbstractArrayElementVisitor implements ArrayStatisticVisitor {
        int count;

        @Override
        public void visit(boolean v) {
            count += v ? 0 : 1;
        }

        @Override
        public void visit(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return "count of false";
        }

        @Override
        public String getStatistic() {
            return String.valueOf(count);
        }
    }

    public static class SumLong extends AbstractArrayElementVisitor implements ArrayStatisticVisitor {
        long sum;

        @Override
        public String getName() {
            return "sum";
        }

        @Override
        public String getStatistic() {
            return String.valueOf(sum);
        }

        @Override
        public void visit(long v) {
            sum += v;
        }

        @Override
        public void visit(boolean v) {
            visit(v ? 1 : 0);
        }

        @Override
        public void visit(double v) {
            visit((long) v);
        }

        @Override
        public void visit(float v) {
            visit((long) v);
        }

        @Override
        public void visit(int v) {
            visit((long) v);
        }

        @Override
        public void visit(short v) {
            visit((long) v);
        }

        @Override
        public void visit(char v) {
            visit((long) v);
        }

        @Override
        public void visit(byte v) {
            visit((long) v);
        }
    }
}
