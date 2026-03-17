package org.violetlib.geom;

import org.junit.Test;

import static org.junit.Assert.*;

public class GeneralRoundRectangleTest
{
    // A 100x100 rectangle at (0,0) with uniform arc width/height of 20
    private static GeneralRoundRectangle uniform()
    {
        return new GeneralRoundRectangle(0, 0, 100, 100,
          20, 20, 20, 20, 20, 20, 20, 20);
    }

    // A 100x100 rectangle at (0,0) with no rounding
    private static GeneralRoundRectangle sharp()
    {
        return new GeneralRoundRectangle(0, 0, 100, 100,
          0, 0, 0, 0, 0, 0, 0, 0);
    }

    // A 100x80 rectangle at (10,10) with different arcs per corner
    private static GeneralRoundRectangle asymmetric()
    {
        return new GeneralRoundRectangle(10, 10, 100, 80,
          30, 20,  // tlaw, tlah (top-left)
          10, 40,  // traw, trah (top-right)
          20, 10,  // braw, brah (bottom-right)
          40, 30); // blaw, blah (bottom-left)
    }

    // ---------- contains(x, y) ----------

    @Test
    public void contains_emptyRect_returnsFalse()
    {
        GeneralRoundRectangle rr = new GeneralRoundRectangle(0, 0, 0, 100,
          10, 10, 10, 10, 10, 10, 10, 10);
        assertFalse(rr.contains(0, 50));
    }

    @Test
    public void contains_outsideBounds_returnsFalse()
    {
        GeneralRoundRectangle rr = uniform();
        assertFalse(rr.contains(-1, 50));
        assertFalse(rr.contains(50, -1));
        assertFalse(rr.contains(100, 50));
        assertFalse(rr.contains(50, 100));
    }

    @Test
    public void contains_center_returnsTrue()
    {
        assertTrue(uniform().contains(50, 50));
    }

    @Test
    public void contains_midEdge_returnsTrue()
    {
        GeneralRoundRectangle rr = uniform();
        // Middle of each edge — well inside the body, outside any corner zone
        assertTrue(rr.contains(50, 0));   // top edge center
        assertTrue(rr.contains(50, 99));  // bottom edge center
        assertTrue(rr.contains(0, 50));   // left edge center
        assertTrue(rr.contains(99, 50));  // right edge center
    }

    @Test
    public void contains_cornerOutsideArc_returnsFalse()
    {
        GeneralRoundRectangle rr = uniform();
        // Very close to the corner, outside the elliptical arc
        // Arc radius is 10 (arcWidth=20, half=10). Point (0,0) is the corner.
        // At (1, 1): nx = (1-10)/10 = -0.9, ny = (1-10)/10 = -0.9
        // nx^2 + ny^2 = 0.81 + 0.81 = 1.62 > 1 → outside
        assertFalse(rr.contains(1, 1));
        assertFalse(rr.contains(99, 1));
        assertFalse(rr.contains(99, 99));
        assertFalse(rr.contains(1, 99));
    }

    @Test
    public void contains_cornerInsideArc_returnsTrue()
    {
        GeneralRoundRectangle rr = uniform();
        // Arc radius is 10. Point (5, 5):
        // nx = (5-10)/10 = -0.5, ny = (5-10)/10 = -0.5
        // nx^2 + ny^2 = 0.25 + 0.25 = 0.5 <= 1 → inside
        assertTrue(rr.contains(5, 5));
        assertTrue(rr.contains(95, 5));
        assertTrue(rr.contains(95, 95));
        assertTrue(rr.contains(5, 95));
    }

    @Test
    public void contains_sharpCorners_returnsTrue()
    {
        GeneralRoundRectangle rr = sharp();
        // With no rounding, all corners should be contained
        assertTrue(rr.contains(0, 0));
        assertTrue(rr.contains(99, 0));
        assertTrue(rr.contains(99, 99));
        assertTrue(rr.contains(0, 99));
    }

    @Test
    public void contains_onArcBoundary_returnsTrue()
    {
        GeneralRoundRectangle rr = uniform();
        // Point exactly on the ellipse boundary (nx^2 + ny^2 == 1.0)
        // At the arc center entry point: (10, 0)
        // nx = (10-10)/10 = 0, ny = (0-10)/10 = -1
        // nx^2 + ny^2 = 0 + 1 = 1.0 → on boundary, should be contained (<= 1.0)
        assertTrue(rr.contains(10, 0));
    }

    @Test
    public void contains_asymmetricCorners()
    {
        GeneralRoundRectangle rr = asymmetric();
        // Center is definitely inside
        assertTrue(rr.contains(60, 50));

        // Top-left corner: arc is 30w, 20h → half = 15, 10
        // Corner zone: x < 10+15=25, y < 10+10=20
        // Point (11, 11): nx = (11-25)/15 = -0.933, ny = (11-20)/10 = -0.9
        // nx^2 + ny^2 = 0.871 + 0.81 = 1.681 > 1 → outside
        assertFalse(rr.contains(11, 11));

        // Point (20, 15): nx = (20-25)/15 = -0.333, ny = (15-20)/10 = -0.5
        // nx^2 + ny^2 = 0.111 + 0.25 = 0.361 → inside
        assertTrue(rr.contains(20, 15));
    }

    // ---------- contains(x, y, w, h) ----------

    @Test
    public void containsRect_fullyInside_returnsTrue()
    {
        assertTrue(uniform().contains(20, 20, 60, 60));
    }

    @Test
    public void containsRect_overlapCorner_returnsFalse()
    {
        // Small rect in the very corner — all 4 corner points are outside the arc
        assertFalse(uniform().contains(0, 0, 2, 2));
    }

    @Test
    public void containsRect_empty_returnsFalse()
    {
        assertFalse(uniform().contains(50, 50, 0, 10));
        assertFalse(uniform().contains(50, 50, 10, 0));
    }

    // ---------- intersects(x, y, w, h) ----------

    @Test
    public void intersects_emptyShape_returnsFalse()
    {
        GeneralRoundRectangle rr = new GeneralRoundRectangle(0, 0, 0, 100,
          10, 10, 10, 10, 10, 10, 10, 10);
        assertFalse(rr.intersects(0, 0, 50, 50));
    }

    @Test
    public void intersects_emptyQuery_returnsFalse()
    {
        assertFalse(uniform().intersects(50, 50, 0, 10));
        assertFalse(uniform().intersects(50, 50, 10, 0));
    }

    @Test
    public void intersects_noOverlap_returnsFalse()
    {
        GeneralRoundRectangle rr = uniform();
        assertFalse(rr.intersects(200, 200, 10, 10));
        assertFalse(rr.intersects(-20, 50, 10, 10));
        assertFalse(rr.intersects(110, 50, 10, 10));
    }

    @Test
    public void intersects_middleBand_returnsTrue()
    {
        GeneralRoundRectangle rr = uniform();
        // Rect that overlaps the horizontal middle band (extends from above into the top edge)
        assertTrue(rr.intersects(40, -10, 20, 15));
        // Rect that overlaps the vertical middle band (extends from left into the left edge)
        assertTrue(rr.intersects(-10, 40, 15, 20));
    }

    @Test
    public void intersects_fullyInside_returnsTrue()
    {
        assertTrue(uniform().intersects(20, 20, 60, 60));
    }

    @Test
    public void intersects_cornerOverlap_returnsTrue()
    {
        GeneralRoundRectangle rr = uniform();
        // A rect that overlaps the corner zone and touches the arc
        // Arc radius = 10 at top-left corner.
        // Rect from (3,3) to (13,13) — extends past the arc center (10,10) so should intersect
        assertTrue(rr.intersects(3, 3, 10, 10));
    }

    @Test
    public void intersects_cornerMiss_returnsFalse()
    {
        GeneralRoundRectangle rr = uniform();
        // A tiny rect in the very corner that doesn't touch the arc.
        // Arc center is at (10, 10). Rect (0,0)-(1,1).
        // Nearest point to center (10,10) is (1,1).
        // nx = (1-10)/10 = -0.9, ny = (1-10)/10 = -0.9
        // nx^2 + ny^2 = 1.62 > 1 → miss
        assertFalse(rr.intersects(0, 0, 1, 1));
    }

    @Test
    public void intersects_allCornerMisses()
    {
        GeneralRoundRectangle rr = uniform();
        assertFalse(rr.intersects(0, 0, 1, 1));       // top-left
        assertFalse(rr.intersects(99, 0, 1, 1));       // top-right
        assertFalse(rr.intersects(99, 99, 1, 1));      // bottom-right
        assertFalse(rr.intersects(0, 99, 1, 1));       // bottom-left
    }

    @Test
    public void intersects_sharpCorners_returnsTrue()
    {
        GeneralRoundRectangle rr = sharp();
        // With no rounding, any overlap with the bounding rect should intersect
        assertTrue(rr.intersects(0, 0, 1, 1));
        assertTrue(rr.intersects(99, 99, 1, 1));
    }

    @Test
    public void intersects_spanningEntireShape_returnsTrue()
    {
        assertTrue(uniform().intersects(-10, -10, 120, 120));
    }

    @Test
    public void intersects_touchingEdge_returnsTrue()
    {
        // Rect that just touches the top edge in the middle (no corner involvement)
        assertTrue(uniform().intersects(40, -5, 20, 6));
    }

    @Test
    public void intersects_justOutside_returnsFalse()
    {
        // Rect that is just outside the right edge
        assertFalse(uniform().intersects(100, 40, 10, 20));
    }

    @Test
    public void intersects_asymmetricCorners()
    {
        GeneralRoundRectangle rr = asymmetric();

        // Center overlap
        assertTrue(rr.intersects(50, 40, 20, 20));

        // Top-left corner miss: arc is 30w, 20h → half = 15, 10
        // Corner at (10, 10). Tiny rect at (10, 10) size 1x1.
        // Nearest point to ellipse center (25, 20) is (11, 11).
        // nx = (11-25)/15 = -0.933, ny = (11-20)/10 = -0.9
        // nx^2 + ny^2 = 0.871 + 0.81 = 1.681 > 1 → miss
        assertFalse(rr.intersects(10, 10, 1, 1));

        // Bottom-right corner: arc is 20w, 10h → half = 10, 5
        // Corner at (110, 90). Tiny rect at (109, 89) size 1x1.
        // Nearest point to ellipse center (100, 85) is (109, 85).
        // nx = (109-100)/10 = 0.9, ny = 0
        // nx^2 + ny^2 = 0.81 → inside, so intersects
        assertTrue(rr.intersects(109, 85, 1, 1));
    }
}
