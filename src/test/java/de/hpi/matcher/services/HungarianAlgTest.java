package de.hpi.matcher.services;

import de.hpi.machinelearning.HungarianAlgorithm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class HungarianAlgTest {

    @Test
    public void testHungarian() {
        double[][] matchScores = {
                {0.1, 0.4, 0.3, 0.1},
                {0.1, 0.3, 0.3, 0.2},
                {0.1, 0.2, 0.4, 0.3},
                {0.1, 0.2, 0.3, 0.4}
        };
        int[][] matchIndices = HungarianAlgorithm.hgAlgorithm(matchScores, "max");
        for(int[] row :matchIndices)

        {
            for (int value : row) {
                System.out.println(value);
            }
        }
        int[][]  arrayToBe = {
                {0, 1},
                {1, 0},
                {2, 2},
                {3, 3}};
        assertEquals(matchIndices, arrayToBe);
    }

}
