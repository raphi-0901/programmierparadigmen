import java.util.Iterator;
import java.util.NoSuchElementException;

public class Nest implements Formicarium {
    private final Compatibility compatibility;

    /**
     * Initializes a Nest.
     */
    public Nest() {
        int[] size = new int[2];
        size[1] = 20;

        int[] temperature = new int[2];
        temperature[0] = 5;
        temperature[1] = 30;

        int[] humidity = new int[2];
        humidity[0] = 30;
        humidity[1] = 80;

        compatibility = new Compatibility(size, temperature, humidity, ETime.WEEK);
    }

    /**
     * @return the Compatibility object of this
     */
    @Override
    public Compatibility compatibility() {
        return compatibility;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<FormicariumPart> iterator() {
        return new NestIterator(this);
    }

    // simple iterator which simply returns the given item once.
    private static class NestIterator implements Iterator<FormicariumPart> {
        private boolean hasNext;
        private final FormicariumPart item;

        public NestIterator(FormicariumPart item) {
            this.hasNext = true;
            this.item = item;
        }

        /**
         * Returns true if the iteration has more elements.
         *
         * @return true, if there are items left.
         */
        @Override
        public boolean hasNext() {
            return hasNext;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public FormicariumPart next() throws NoSuchElementException {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            hasNext = false;
            return item;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nest that = (Nest) o;
        return this.compatibility().equals(that.compatibility());
    }

    @Override
    public int hashCode() {
        return this.compatibility().hashCode();
    }
}
