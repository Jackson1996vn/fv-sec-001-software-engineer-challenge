package com.campaign;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CampaignMetricsTest {

    @Test
    void newMetricsHaveZeroTotals() {
        CampaignMetrics m = new CampaignMetrics();
        assertEquals(0, m.getTotalImpressions());
        assertEquals(0, m.getTotalClicks());
        assertEquals(0.0, m.getTotalSpend(), 0.001);
        assertEquals(0, m.getTotalConversions());
    }

    @Test
    void addRowAccumulatesValues() {
        CampaignMetrics m = new CampaignMetrics();
        m.addRow(1000, 50, 12.50, 3);
        m.addRow(2000, 100, 25.00, 7);
        assertEquals(3000, m.getTotalImpressions());
        assertEquals(150, m.getTotalClicks());
        assertEquals(37.50, m.getTotalSpend(), 0.001);
        assertEquals(10, m.getTotalConversions());
    }

    @Test
    void ctrIsClicksDividedByImpressions() {
        CampaignMetrics m = new CampaignMetrics();
        m.addRow(3000, 150, 37.50, 10);
        assertEquals(0.05, m.getCtr(), 0.0001);
    }

    @Test
    void ctrIsZeroWhenNoImpressions() {
        CampaignMetrics m = new CampaignMetrics();
        assertEquals(0.0, m.getCtr(), 0.0001);
    }

    @Test
    void cpaIsSpendDividedByConversions() {
        CampaignMetrics m = new CampaignMetrics();
        m.addRow(3000, 150, 37.50, 10);
        assertNotNull(m.getCpa());
        assertEquals(3.75, m.getCpa(), 0.001);
    }

    @Test
    void cpaIsNullWhenZeroConversions() {
        CampaignMetrics m = new CampaignMetrics();
        m.addRow(1000, 50, 10.00, 0);
        assertNull(m.getCpa());
    }
}
