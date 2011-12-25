package org.hibernate.envers.test.integration.collection.norevision;
import java.util.Arrays;
import java.util.List;

public class CollectionChangeRevisionTest extends AbstractCollectionChangeTest {       
    @Override
    protected String getCollectionChangeValue() {
        return "true";
    }

    @Override
    protected List<Long> getExpectedPersonRevisions() {
        return Arrays.asList(1L, 3L);
    }
}
