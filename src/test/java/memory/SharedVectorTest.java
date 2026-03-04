package memory;

import java.util.concurrent.locks.ReadWriteLock;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedVectorTest {

    // get
    @Test
    void testGet_ValidIndex() {
        SharedVector vector = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);

        assertEquals(1.0, vector.get(0));
        assertEquals(2.0, vector.get(1));
        assertEquals(3.0, vector.get(2));
    }

    @Test
    void testGet_NegativeIndexThrows() {
        SharedVector vector = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);

        assertThrows(IndexOutOfBoundsException.class, () -> vector.get(-1));
    }

    @Test
    void testGet_TooLargeIndexThrows() {
        SharedVector vector = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);

        assertThrows(IndexOutOfBoundsException.class, () -> vector.get(3));
    }


    // length
    @Test
    void testLength_Normal() {
        SharedVector vector = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);

        assertEquals(3, vector.length());
    }
    @Test
    void testLength_Empty() {
        SharedVector vector = new SharedVector(new double[]{}, VectorOrientation.ROW_MAJOR);

        assertEquals(0, vector.length());
    }

    // getOrientation
    @Test
    void testGetOrientation_RowMajor() {
        SharedVector vector = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);
        assertEquals(VectorOrientation.ROW_MAJOR, vector.getOrientation());
    }

    @Test
    void testGetOrientation_ColumnMajor() {
        SharedVector vector = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.COLUMN_MAJOR);
        assertEquals(VectorOrientation.COLUMN_MAJOR, vector.getOrientation());
    }

    // transpose
    @Test
    void testTranspose_RowToColumn() {
        SharedVector vector = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        vector.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, vector.getOrientation());
    }

    @Test
    void testTranspose_ColumnToRow() {
        SharedVector vector = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.COLUMN_MAJOR);
        vector.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, vector.getOrientation());
    }

    @Test
    void testTranspose_DoubleReturnsOriginal() {
        SharedVector vector = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        vector.transpose();
        vector.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, vector.getOrientation());
    }

    // add
    @Test
    void testAdd_ValidSize() {
        SharedVector v1 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{4, 5, 6}, VectorOrientation.ROW_MAJOR);

        v1.add(v2);

        assertArrayEquals(new double[]{5, 7, 9}, v1.getVector());
    }

    @Test
    void testAdd_WrongSize() {
        SharedVector v1 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{4, 5}, VectorOrientation.ROW_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> {
            v1.add(v2);
        });
    }

    //negate
    @Test
    void testNegate() {
        SharedVector vector = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.ROW_MAJOR);
        vector.negate();
        assertArrayEquals(new double[]{-1.0, -2.0, -3.0}, vector.getVector());
    }

    //dot
    @Test
    void testDot_validValues() {
        SharedVector vector = new SharedVector(new double[]{-1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector other = new SharedVector(new double[]{-4, 5, -6}, VectorOrientation.ROW_MAJOR);

        double result = vector.dot(other);
        assertEquals(-4, result);
    }

    @Test
    void testDot_DifferentLengthsThrows() {
        SharedVector vector = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedVector other = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> vector.dot(other));
    }

    @Test
    void testDot_EmptyVectors() {
        SharedVector vector = new SharedVector(new double[]{}, VectorOrientation.ROW_MAJOR);
        SharedVector other = new SharedVector(new double[]{}, VectorOrientation.ROW_MAJOR);

        double result = vector.dot(other);
        assertEquals(0, result);
    }

    //vecMatMul
    @Test
    void testVecMatMul_ColumnMajor() {
        SharedVector vector = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedMatrix matrix = new SharedMatrix(new double[][]{
            {0, 3},
            {2, -4},
            {3, 2}
        });

        double[] expected = {13, 1};
        vector.vecMatMul(matrix);
        assertArrayEquals(expected, vector.getVector());
    }

    @Test
    void testVecMatMul_RowMajor() {
        SharedVector vector = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(new double[][]{
            {0, 2, 3},
            {3, -4, 2}
        });

        double[] expected = {13, 1};
        vector.vecMatMul(matrix);
        assertArrayEquals(expected, vector.getVector());
    }

    @Test
    void testVecMatMul_ColumnMajorVector() {
        SharedVector vector = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.COLUMN_MAJOR);
        SharedMatrix matrix = new SharedMatrix(new double[][]{
            {0, 3},
            {2, -4},
            {3, 2}
        });

        assertThrows(IllegalArgumentException.class, () -> {
           vector.vecMatMul(matrix);
        });
    }

     @Test
    void testVecMatMul_EmptyMatrix() {
        SharedVector vector = new SharedVector(new double[]{}, VectorOrientation.ROW_MAJOR);
        SharedMatrix matrix = new SharedMatrix();

        double[] expected = {};
        vector.vecMatMul(matrix);
        assertArrayEquals(expected, vector.getVector());
    }
}
