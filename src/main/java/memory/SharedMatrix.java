package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // Initialize empty matrix
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        if (matrix == null)
            throw new IllegalArgumentException("Matrix cannot be null");

        this.vectors = new SharedVector[matrix.length];
        int columnLength = (matrix.length == 0) ? 0 : matrix[0].length;

        for (int i = 0; i < matrix.length; i++) {
            if (columnLength != matrix[i].length)
                throw new IllegalArgumentException("Matrix vectors cannot be of different lengths");
            
            vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadRowMajor(double[][] matrix) {
        if (matrix == null)
            throw new IllegalArgumentException("Matrix cannot be null");
        
        SharedVector[] newVectors = new SharedVector[matrix.length];
        int columnLength = (matrix.length == 0) ? 0 : matrix[0].length;

        for (int i = 0; i < matrix.length; i++) {
            if (columnLength != matrix[i].length)
                throw new IllegalArgumentException("Matrix vectors cannot be of different lengths");
            
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }

        this.vectors = newVectors;
    }

    public void loadColumnMajor(double[][] matrix) {
        // replace internal data with new column-major matrix
        if (matrix == null)
            throw new IllegalArgumentException("Matrix cannot be null");
        
        SharedVector[] newVectors = new SharedVector[matrix.length];
        int columnLength = (matrix.length == 0) ? 0 : matrix[0].length;

        for (int i = 0; i < matrix.length; i++) {
            if (columnLength != matrix[i].length)
                throw new IllegalArgumentException("Matrix vectors cannot be of different lengths");
            
            newVectors[i] = new SharedVector(matrix[i], VectorOrientation.COLUMN_MAJOR);
        }

        this.vectors = newVectors;
    }

    public double[][] readRowMajor() {
        // return matrix contents as a row-major double[][]
        acquireAllVectorReadLocks(vectors);
        try {
            if (getOrientation() == VectorOrientation.ROW_MAJOR) {
                int rows = length();
                if (rows == 0) {
                    return new double[][] {};
                }
                double[][] newMatrix = new double[rows][];
                for (int i = 0; i < newMatrix.length; i++) {
                    newMatrix[i] = get(i).getVector();
                }
                return newMatrix;
            } else {
                int columns = length();
                if (columns == 0) {
                    return new double[][] {};
                }
                double[][] newMatrix = new double[vectors[0].length()][columns];
                for (int i = 0; i < newMatrix.length; i++) {
                    for (int j = 0; j < newMatrix[i].length; j++) {
                        newMatrix[i][j] = get(j).get(i);
                    }
                }
                return newMatrix;
            }
        } finally {
            releaseAllVectorReadLocks(vectors);
        }
    }

    public SharedVector get(int index) {
        // return vector at index
        if (index < 0 || index >= vectors.length)
            throw new IndexOutOfBoundsException("Index out of range");

        return vectors[index];
    }

    public int length() {
        // return number of stored vectors
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        // acquireAllVectorReadLocks(vectors);
        // try {
        if (vectors.length == 0)
            return VectorOrientation.ROW_MAJOR;

        return vectors[0].getOrientation();
        //     for (int i = 0; i < vectors.length; i++) {
        //         if (vectors[i].getOrientation() != orientation)
        //             throw new IllegalArgumentException("Matrix vectors of wrong orientation");
        //     }
        //     return orientation;
        // } finally {
        //     releaseAllVectorReadLocks(vectors);
        // }
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        if (vecs != null) {
            for (SharedVector vector : vecs) {
                if (vector != null) {
                    vector.readLock();
                }
            }
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        if (vecs != null) {
            for (SharedVector vector : vecs) {
                if (vector != null) {
                    vector.readUnlock();
                }
            }
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        if (vecs != null) {
            for (SharedVector vector : vecs) {
                if (vector != null) {
                    vector.writeLock();
                }
            }
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        if (vecs != null) {
            for (SharedVector vector : vecs) {
                if (vector != null) {
                    vector.writeUnlock();
                }
            }
        }
    }
}
