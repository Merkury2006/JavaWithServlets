package vsu.cs.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.cs.domain.WorkOrderItem;
import vsu.cs.repository.impl.InMemoryWorkOrderItemRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryWorkOrderItemRepositoryTest {

    private InMemoryWorkOrderItemRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryWorkOrderItemRepository();
    }

    @Test
    void findByWorkOrderId_ok() {
        repo.save(new WorkOrderItem(1L, "Работа", 100.0, 1));
        repo.save(new WorkOrderItem(1L, "Деталь", 200.0, 2));
        repo.save(new WorkOrderItem(2L, "Другое", 50.0, 1));

        List<WorkOrderItem> items = repo.findByWorkOrderId(1L);
        assertEquals(2, items.size());
    }

    @Test
    void deleteByWorkOrderId_removesOnlyMatching() {
        repo.save(new WorkOrderItem(1L, "Работа", 100.0, 1));
        repo.save(new WorkOrderItem(1L, "Деталь", 200.0, 2));
        repo.save(new WorkOrderItem(2L, "Другое", 50.0, 1));

        repo.deleteByWorkOrderId(1L);

        assertTrue(repo.findByWorkOrderId(1L).isEmpty());
        assertEquals(1, repo.findByWorkOrderId(2L).size());
    }

    @Test
    void deleteByWorkOrderId_noMatch_noop() {
        repo.save(new WorkOrderItem(1L, "Работа", 100.0, 1));
        repo.deleteByWorkOrderId(999L);
        assertEquals(1, repo.findByWorkOrderId(1L).size());
    }

    @Test
    void subtotal_isPriceTimesQuantity() {
        WorkOrderItem i = new WorkOrderItem(1L, "Работа", 150.0, 3);
        assertEquals(450.0, i.getSubtotal(), 0.001);
    }
}