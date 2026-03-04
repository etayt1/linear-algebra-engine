package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector = vector.clone();
        this.orientation = orientation;
    }

    public double get(int index) {
        readLock();
        try {
            if (index < 0 || index >= vector.length) 
                throw new IndexOutOfBoundsException("Index out of range");
            return vector[index];
        } finally {
            readUnlock();
        }
    }

    // Helper function
    public double[] getVector() {
        readLock();
        try {
            return vector.clone();
        } finally {
            readUnlock();
        }
    }
    

    public int length() {
        readLock();
        try {
            return vector.length;
        } finally {
            readUnlock();
        }
    }

    public VectorOrientation getOrientation() {
        readLock();
        try {
            return orientation;
        } finally {
            readUnlock();
        }
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        writeLock();
        try {
            orientation = (orientation == VectorOrientation.COLUMN_MAJOR) 
                ? VectorOrientation.ROW_MAJOR 
                : VectorOrientation.COLUMN_MAJOR;
        } finally {
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        writeLock();
        other.readLock();
        try {
            if (this.length() != other.length())
                throw new IllegalArgumentException("Vectors must have the same length");

            for (int i = 0; i < vector.length; i++) {
                vector[i] += other.get(i);
            }
        } finally {
            writeUnlock();
            other.readUnlock();
        }
    }

    public void negate() {
        writeLock();
        try {
            for (int i = 0; i < vector.length; i++) {
                vector[i] = -vector[i];
            }
        } finally {
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        this.readLock();
        other.readLock();
        try {
            if (this.length() != other.length())
                throw new IllegalArgumentException("Vectors must have the same length");
            double sum = 0;
            for (int i = 0; i < this.length(); i++) {
                sum += this.get(i) * other.get(i);
            }
            return sum;

        } finally {
            this.readUnlock();
            other.readUnlock();
        }
    }

    public void vecMatMul(SharedMatrix matrix) {
        writeLock();
        try {
            if (orientation == VectorOrientation.COLUMN_MAJOR)
                throw new IllegalArgumentException("Vector must be row-major");

            double[] newVector;

            if (matrix.getOrientation() == VectorOrientation.COLUMN_MAJOR) {
                // unnecessary?
                int length = matrix.length();
                newVector = new double[length];
                for (int i = 0; i < length; i++) {
                    newVector[i] = this.dot(matrix.get(i));
                }
            } else {
                int rows = matrix.length();
                if (rows != 0) {
                    int columns = matrix.get(0).length();

                    if (rows != vector.length)
                        throw new IllegalArgumentException("Vector must have the same length as the columns");
                    
                    newVector = new double[columns];

                    for (int j = 0; j < rows; j++) {
                        if (matrix.get(j).length() != columns)
                            throw new IllegalArgumentException("Rows must be of same length");
                    }
                    
                    for (int i = 0; i < columns; i++) {
                        double sum = 0;
                        for (int j = 0; j < rows; j++) {
                            sum += vector[j] * matrix.get(j).get(i);
                        }
                        newVector[i] = sum;
                    }
                } else {
                    newVector = new double[]{};
                }
                
            }
            this.vector = newVector;
                
        } finally {
            writeUnlock();
        }
    }
}
