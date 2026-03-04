package memory;

import java.util.concurrent.locks.ReadWriteLock;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {
    @Test
    void readRowMajor_RowMajorMatrix() {
        double[][] rowMajor = {
            {1, 2, 3},
            {4, 5, 6}
        };

        SharedMatrix matrix = new SharedMatrix();
        matrix.loadRowMajor(rowMajor);

        double[][] result = matrix.readRowMajor();

        assertArrayEquals(rowMajor, result);
    }

    @Test
    void readRowMajor_ColumnMajorMatrix() {
        double[][] columnMajor = {
            {1, 3, 5},
            {2, 4, 6}
        };

        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(columnMajor);

        double[][] expected = {
            {1, 2},
            {3, 4},
            {5, 6}
        };

        double[][] result = matrix.readRowMajor();

        assertArrayEquals(expected, result);
    }

    @Test
    void testJaggedArray_Constructor() {
        // 1st row len 2, 2nd row len 1
        double[][] jagged = {
            {1.0, 2.0},
            {3.0}
        };

        // matrix must reject uneven rows
        assertThrows(IllegalArgumentException.class, () -> {
            new SharedMatrix(jagged);
        });
    }

    @Test
    void testJaggedArray_Load() {
        double[][] jagged = { {1.0}, {2.0, 3.0} };
        SharedMatrix matrix = new SharedMatrix();

        // load method must scream too
        assertThrows(IllegalArgumentException.class, () -> {
            matrix.loadRowMajor(jagged);
        });
    }

    @Test
    void testNullInput() {
        // null is big no-no
        assertThrows(IllegalArgumentException.class, () -> {
            new SharedMatrix(null);
        });

        SharedMatrix matrix = new SharedMatrix();
        
        // can't load nothing
        assertThrows(IllegalArgumentException.class, () -> {
            matrix.loadColumnMajor(null);
        });
    }

    @Test
    void testEmptyMatrix() {
        // empty data
        double[][] empty = {};
        
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadRowMajor(empty);

        // check size 0
        assertEquals(0, matrix.length());
        
        // check read back empty
        double[][] result = matrix.readRowMajor();
        assertEquals(0, result.length);
    }

    @Test
    void testIndexOutOfBounds() {
        // setup normal matrix
        double[][] data = {{1, 2}, {3, 4}};
        SharedMatrix matrix = new SharedMatrix(data);

        // check negative index crash
        assertThrows(IndexOutOfBoundsException.class, () -> {
            matrix.get(-1);
        });

        // check too big index crash
        assertThrows(IndexOutOfBoundsException.class, () -> {
            matrix.get(5); 
        });
    }

    @Test
    void testOrientationConsistency() {
        // dummy data
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix m = new SharedMatrix();

        // 1. load row mode
        m.loadRowMajor(data);
        
        // check flag is row
        assertEquals(VectorOrientation.ROW_MAJOR, m.getOrientation());

        // 2. overwrite with col mode
        m.loadColumnMajor(data);

        // check flag swapped to col
        assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());
    }
}