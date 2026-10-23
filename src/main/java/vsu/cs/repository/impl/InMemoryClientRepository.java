package vsu.cs.repository.impl;

import vsu.cs.domain.Client;
import vsu.cs.repository.ClientRepository;

import java.util.concurrent.atomic.AtomicLong;

public class InMemoryClientRepository extends InMemoryRepository<Client, Long> implements ClientRepository {
    public InMemoryClientRepository() {
        super(new AtomicLong(0)::incrementAndGet);
    }
}
