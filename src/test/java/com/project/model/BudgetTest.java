package com.project.model;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class BudgetTest {

    @Test
    void shouldCreateBudgetWithDefaultDates() {
        Budget budget = new Budget();

        assertNotNull(budget.getStartDate());
        assertNotNull(budget.getEndDate());
        assertTrue(budget.getStartDate().before(budget.getEndDate()));
    }

    @Test
    void shouldCreateBudgetWithParameters() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 86400000); // +1 day

        Budget budget = new Budget("Food", 500.0, start, end, "Monthly food budget");

        assertEquals("Food", budget.getCategory());
        assertEquals(500.0, budget.getAmount());
        assertEquals(start, budget.getStartDate());
        assertEquals(end, budget.getEndDate());
        assertEquals("Monthly food budget", budget.getDescription());
    }

    @Test
    void shouldSetAndGetId() {
        Budget budget = new Budget();
        budget.setId(123);

        assertEquals(123, budget.getId());
    }
}